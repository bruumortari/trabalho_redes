public abstract class AbstractRIP implements UnicastServiceUserInterface, Runnable{
    protected UnicastServiceInterface usi;
    protected short idNode;

    public void SetUSI(UnicastServiceInterface usi) {
        this.usi = usi;
    }

    public AbstractRIP(short id){
        this.idNode = id;
    }
}
