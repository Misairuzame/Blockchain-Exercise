package unichain;

import java.util.ArrayList;
import java.util.Date;

public class Blocco {

    public String hash;
    public String previousHash;
    public long timestamp;
    public int nonce = 0;
    public String merkleRoot;
    public ArrayList<Transaction> transactions = new ArrayList<>();

    public Blocco(String previousHash) {
        this.previousHash = previousHash;
        this.timestamp = new Date().getTime();
        this.hash = calculateHash();
    }

    public String calculateHash() {
        return StringUtil.applySHA256(
                previousHash + timestamp + nonce + merkleRoot);
    }

    public void mineBlock(int difficolta) {
        //Calcola il merkle root
        merkleRoot = StringUtil.getMerkleRoot(transactions);

        StringBuilder numOfZeros = new StringBuilder();
        for (int i=0; i<difficolta; i++) numOfZeros.append(0);

        while (true) {
            hash = calculateHash();
            if (hash.startsWith(numOfZeros.toString())) {
                System.out.println("Block mined: " + hash);
                break;
            }
            nonce++;
        }
    }

    public boolean addTransaction(Transaction transaction) {
        //Verifica che la transazione sia valida
        if (transaction == null) return false;
        //Escludiamo dal controllo il blocco genesi: le transazioni non hanno input
        if (!previousHash.equals("0")) {
            if (!transaction.processTransaction()) {
                System.out.println("Processamento della transazione fallito, transazione scartata.");
                return false;
            }
        }
        transactions.add(transaction);
        System.out.println("Transazione aggiunta con successo al blocco");
        return true;
    }

}
