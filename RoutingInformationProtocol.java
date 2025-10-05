public class RoutingInformationProtocol implements UnicastServiceUserInterface, Runnable{
    private UnicastServiceInterface usi;
    private short target;
    private String message;

    public RoutingInformationProtocol(UnicastServiceInterface usi, short target, String message){
        this.usi = usi;
        this.target = target;
        this.message = message;
    }
    @Override
    public void UPDataInd(short source, String message) {
        System.out.println("Mensagem recebida de "+source+" : "+message);
    }
    @Override
    public void run() {
        if(usi.UPDataReq(target, message)){
            System.out.println("mensagem enviada para :" + target);
        }else{
            System.out.println("mensagem nao enviada :(");
        }
    }
}
