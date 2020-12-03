package unichain;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UniChain {

    public static List<Blocco> blockchain = new ArrayList<>();
    public static HashMap<String, TransactionOutput> UTXOs = new HashMap<>();
    // ^Lista delle transazioni non spese, in modo che possano essere verificate pubblicamente^
    public static final int difficolta = 5;
    public static Wallet walletA;
    public static Wallet walletB;
    public static float minimumTransaction = 1.5f;
    public static Transaction genesisTransaction;

    public static void main(String[] args) {
        //Imposta Bouncycastle come Security Provider
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        walletA = new Wallet();
        walletB = new Wallet();

        Wallet coinbase = new Wallet();
        genesisTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 100, null);
        genesisTransaction.generateSignature(coinbase.privateKey);
        genesisTransaction.transactionId = "0";
        genesisTransaction.outputs.add(
                new TransactionOutput(genesisTransaction.recipient, genesisTransaction.value,
                        genesisTransaction.transactionId)
        );

        UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        Blocco genesisBlock = new Blocco("0");
        genesisBlock.addTransaction(genesisTransaction);

        addBock(genesisBlock);

        System.out.println("-----------------------\n" +
                "Transazione 1\n" +
                "-----------------------");
        Blocco blocco1 = new Blocco(genesisBlock.hash);
        System.out.println("WalletA possiede: "+walletA.getBalance());
        System.out.println("WalletB possiede: "+walletB.getBalance());
        System.out.println("A prova ad inviare a B 40 coin");
        blocco1.addTransaction(
                walletA.sendFunds(walletB.publicKey, 40)
        );
        addBock(blocco1);
        System.out.println("WalletA possiede: "+walletA.getBalance());
        System.out.println("WalletB possiede: "+walletB.getBalance());

        System.out.println("-----------------------\n" +
                "Transazione 2\n" +
                "-----------------------");
        Blocco blocco2 = new Blocco(blocco1.hash);
        System.out.println("WalletA possiede: "+walletA.getBalance());
        System.out.println("WalletB possiede: "+walletB.getBalance());
        System.out.println("A prova ad inviare a B 80 coin");
        blocco2.addTransaction(
                walletA.sendFunds(walletB.publicKey, 80)
        );
        addBock(blocco2);
        System.out.println("WalletA possiede: "+walletA.getBalance());
        System.out.println("WalletB possiede: "+walletB.getBalance());

        System.out.println("-----------------------\n" +
                "Transazione 3\n" +
                "-----------------------");
        Blocco blocco3 = new Blocco(blocco2.hash);
        System.out.println("WalletA possiede: "+walletA.getBalance());
        System.out.println("WalletB possiede: "+walletB.getBalance());
        System.out.println("B prova ad inviare a A 20 coin");
        blocco3.addTransaction(
                walletB.sendFunds(walletA.publicKey, 20)
        );
        addBock(blocco3);
        System.out.println("WalletA possiede: "+walletA.getBalance());
        System.out.println("WalletB possiede: "+walletB.getBalance());

        System.out.println("***********FINE TRANSAZIONI***********");

        isChainValid();

    }

    public static boolean isChainValid() {
        Blocco currentBlock;
        Blocco previousBlock;
        String hashTaget = new String(new char[difficolta]).replace('\0', '0');
        HashMap<String, TransactionOutput> tempUTXOs = new HashMap<>();
        tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
        for (int i=1; i<blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i-1);
            //L'hash del blocco corrente è corretto?
            if (!currentBlock.hash.equals(currentBlock.calculateHash())) {
                System.out.println("L'hash del blocco corrente è sbagliato");
                return false;
            }
            //L'hash del blocco precedente corrisponde?
            if(!currentBlock.previousHash.equals(previousBlock.hash)) {
                System.out.println("L'hash del blocco precedente è sbagliato");
                return false;
            }
            //Il blocco è stato minato (PoW)?
            if(!currentBlock.hash.startsWith(hashTaget)) {
                System.out.println("Questo blocco non è stato minato");
                return false;
            }
            //Per ogni blocco verifico tutte le transazioni
            //Verifico firme, input==output, che non siano già state spese,
            //che mittente e destinatario siano corretti
            TransactionOutput tempOutput;
            for(int t=0; t<currentBlock.transactions.size(); t++) {
                Transaction currentTransaction = currentBlock.transactions.get(t);
                if(!currentTransaction.verifySignature()) {
                    System.out.println("La firma della transazione "+t+" non è valida");
                    return false;
                }
                if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
                    System.out.println("Gli input non sono uguali agli output della transazione "+t);
                    return false;
                }
                for(TransactionInput input : currentTransaction.inputs) {
                    tempOutput = tempUTXOs.get(input.transactionOutputId);
                    if(tempOutput == null) {
                        System.out.println("Manca la transazione riferita nella transazione "+t);
                        return false;
                    }
                    if(input.UTXO.value != tempOutput.value) {
                        System.out.println("Il valore della transazione riferita nella transazione "+t+
                                " non è valido");
                        return false;
                    }
                    tempUTXOs.remove(input.transactionOutputId);
                }
                for(TransactionOutput output : currentTransaction.outputs) {
                    tempUTXOs.put(output.id, output);
                }
                if(currentTransaction.outputs.get(0).recipient != currentTransaction.recipient) {
                    System.out.println("La transazione "+t+" invia monete a un destinatario sbagliato");
                    return false;
                }
                if(currentTransaction.outputs.get(1).recipient != currentTransaction.sender) {
                    System.out.println("L'output della transazione "+t+" contiene un mittente sbagliato");
                    return false;
                }
            }
        }
        System.out.println("La blockchain è valida");
        return true;
    }

    public static void addBock(Blocco blockToAdd) {
        System.out.println("Mining block...");
        blockToAdd.mineBlock(difficolta);
        blockchain.add(blockToAdd);
    }

}
