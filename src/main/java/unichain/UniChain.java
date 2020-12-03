package unichain;

import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

public class UniChain {

    public static List<Blocco> blockchain = new ArrayList<>();
    public static final int difficolta = 5;

    public static void main(String[] args) {
        Blocco genesisBlock = new Blocco("Primo blocco", "0");
        blockchain.add(genesisBlock);
        System.out.println("Mining block 1...");
        blockchain.get(0).mineBlock(difficolta);

        Blocco secondBlock = new Blocco("Secondo blocco", genesisBlock.hash);
        blockchain.add(secondBlock);
        System.out.println("Mining block 2...");
        blockchain.get(1).mineBlock(difficolta);

        Blocco thirdBlock = new Blocco("Terzo blocco", secondBlock.hash);
        blockchain.add(thirdBlock);
        System.out.println("Mining block 3...");
        blockchain.get(2).mineBlock(difficolta);

        if (isChainValid()) {
            System.out.println("La blockchain E' VALIDA");
        } else {
            System.out.println("La blockchain NON E' VALIDA");
        }

        String jsonBlockchain = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
        System.out.println("---------------------------------------------------");
        System.out.println("La blockchain è la seguente:");
        System.out.println(jsonBlockchain);
    }

    public static boolean isChainValid() {
        Blocco currentBlock;
        Blocco previousBlock;
        String hashTaget = new String(new char[difficolta]).replace('\0', '0');
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
        }
        return true;
    }

}
