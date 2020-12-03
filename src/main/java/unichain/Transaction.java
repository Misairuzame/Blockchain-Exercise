package unichain;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

public class Transaction {

    public String transactionId; //Hash della transazione
    public PublicKey sender;
    public PublicKey recipient;
    public float value;
    public byte[] signature;

    public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
    public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();

    private static int sequence = 0;

    public Transaction(PublicKey from, PublicKey to, float value, ArrayList<TransactionInput> inputs) {
        this.sender = from;
        this.recipient = to;
        this.value = value;
        this.inputs = inputs;
    }

    private String calculateHash() {
        sequence++; //Incrementato per evitare che due transazioni identiche abbiano hash uguali
        return StringUtil.applySHA256(
                   StringUtil.getStringFromKey(sender) +
                        StringUtil.getStringFromKey(recipient) +
                        value +
                        sequence);
    }

    public void generateSignature(PrivateKey privateKey) {
        String data = StringUtil.getStringFromKey(sender) +
                StringUtil.getStringFromKey(recipient) + value;
        signature = StringUtil.applyECDSASignature(privateKey, data);
    }

    public boolean verifySignature() {
        String data = StringUtil.getStringFromKey(sender) +
                StringUtil.getStringFromKey(recipient) + value;
        return StringUtil.verifyECDSASignature(sender, data, signature);
    }

    public float getInputsValue() {
        float total = 0;
        for(TransactionInput i : inputs) {
            if(i.UTXO == null) continue; //Se non trova la Transaction, passa oltre (evita la doppia spesa)
            total += i.UTXO.value;
        }
        return total;
    }

    public float getOutputsValue() {
        float total = 0;
        for(TransactionOutput o : outputs) {
            total += o.value;
        }
        return total;
    }

    public boolean processTransaction() {
        //verificare la firma
        if (!verifySignature()) {
            System.out.println("Verifica della firma della transazione fallita");
            return false;
        }
        //controllare che le transazioni di
        //input figurino fra le transazioni non spese
        for (TransactionInput i : inputs) {
            i.UTXO = UniChain.UTXOs.get(i.transactionOutputId);
        }

        //verificare che la transazione sia corretta
        if (getInputsValue() < UniChain.minimumTransaction) {
            System.out.println("Valore della transazione troppo piccolo: "+getInputsValue());
            return false;
        }

        //creare le transazioni di output
        float resto = getInputsValue() - value;
        transactionId = calculateHash();
        outputs.add(new TransactionOutput(this.recipient, value, transactionId)); //spesa
        outputs.add(new TransactionOutput(this.sender, resto, transactionId)); //resto

        //aggiungere gli output all' elenco delle
        //transazioni non spese della Blockchain
        for (TransactionOutput o : outputs) {
            UniChain.UTXOs.put(o.id, o);
        }

        //rimuovere le transazioni in input dall' elenco
        //delle transazioni non spese della Blockchain
        for (TransactionInput i : inputs) {
            if (i.UTXO == null) continue;
            UniChain.UTXOs.remove(i.UTXO.id);
        }

        return true;
    }


}
