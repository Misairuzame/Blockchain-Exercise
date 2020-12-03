package unichain;

import java.util.Date;

public class Blocco {

    public String hash;
    public String previousHash;
    public String data;
    public long timestamp;
    public int nonce = 0;

    public Blocco(String data, String previousHash) {
        this.previousHash = previousHash;
        this.data = data;
        this.timestamp = new Date().getTime();
        this.hash = calculateHash();
    }

    public String calculateHash() {
        return StringUtil.applySHA256(previousHash + timestamp + data + nonce);
    }

    public void mineBlock(int difficolta) {
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

}
