package unichain;

/* In una transazione gli ingressi (transactionInput) sono
costituiti dalle uscite di transazioni precedenti (TransactionOutput),
purché non siano state già spese */
public class TransactionInput {
    public String transactionOutputId;
    public TransactionOutput UTXO; //Unspent Transaction Output

    public TransactionInput(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }
}
