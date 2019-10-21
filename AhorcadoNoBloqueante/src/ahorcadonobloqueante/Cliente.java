package ahorcadonobloqueante;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class Cliente {
    
    private static String host =  "127.0.0.1";
    private static int pto = 9600;
    
    
    public static void main(String [] args){
        String dif="";
        String w="";
        int mistakes = 0;
        String word="";
        
        try{
            
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            SocketChannel cl = SocketChannel.open();
            cl.configureBlocking(false);
            Selector sel = Selector.open();
            
            cl.connect(new InetSocketAddress(host,pto));
            cl.register(sel, SelectionKey.OP_CONNECT);
            
            while( true ){
                
                sel.select();
                Iterator<SelectionKey> it = sel.selectedKeys().iterator();
                
                while( it.hasNext() ){
                    SelectionKey k = (SelectionKey)it.next();
                    it.remove();
                    
                    if( k.isConnectable() ){
                        SocketChannel ch = (SocketChannel)k.channel();
                        if(ch.isConnectionPending()){
                            try{
                                ch.finishConnect();
                                System.out.println( "----- AHORCADO -----" );
                                System.out.println( "Elija una dificultad" );
                                System.out.println( "(1) Fácil");
                                System.out.println( "(2) Media");
                                System.out.println( "(3) Imposible\n");
                                
                            }catch(Exception e){
                                e.printStackTrace();
                            }//catch
                        }//if_conectionpending
                        //ch.configureBlocking(false);
                        ch.register(sel, SelectionKey.OP_READ|SelectionKey.OP_WRITE);
                        
                        k.interestOps(SelectionKey.OP_WRITE);
                        continue;
                    }
                    if(k.isWritable()){
                        SocketChannel ch2 = (SocketChannel)k.channel();
                        
                        System.out.print(">>>");
                        String msj = br.readLine();
                        msj=dif+"/"+w+"*"+msj+"%"+word+"#"+mistakes;
                        
                        if ( msj.substring(msj.indexOf("*")+1, msj.indexOf("%")).equals("1") ||  msj.substring(msj.indexOf("*")+1, msj.indexOf("%")).equals("2") ||  msj.substring(msj.indexOf("*")+1, msj.indexOf("%")).equals("3") ) {
                            msj = "<begin>"+msj;
                        }
                        else if(  msj.substring(msj.indexOf("*")+1, msj.indexOf("%")).length() > 1 ){
                            msj = dif+"/"+w+"*"+"-1%"+word+"#"+mistakes;
                            System.out.println("Solo se permite una letra");
                        }
                        
                        ByteBuffer b = ByteBuffer.wrap(msj.getBytes());
                        ch2.write(b);
                        
                        
                        k.interestOps(SelectionKey.OP_READ);
                        continue;
                       
                    } else if(k.isReadable()){
                        
                        SocketChannel ch2 = (SocketChannel)k.channel();
                        ByteBuffer b = ByteBuffer.allocate(2000);
                        b.clear();
                        int n = ch2.read(b);
                        b.flip();
                        String msj = new String(b.array());
                        
                        dif = Character.toString(msj.charAt(0));
                        w = msj.substring(msj.indexOf("/")+1, msj.indexOf("*"));
                        
                        if( msj.contains("!end") ){
                            
                            if( msj.contains("|win") ){
                                mistakes = Integer.parseInt(msj.substring(msj.indexOf("?")+1, msj.indexOf("!")));
                                word = msj.substring(msj.indexOf("*")+1, msj.indexOf("?")-1);
                                
                                printVictory(word);
                                
                                ch2.close();
                                System.exit(0);
                                
                            }
                            else if( msj.contains("|fail") ){
                                
                                word = msj.substring(msj.indexOf("*")+1, msj.indexOf("?")-1);
                                
                                printBody(5);
                                System.out.println("PALABRA / ORACIÓN CORRECTA: " + word);
                                
                                ch2.close();
                                System.exit(0);
                            }
                            
                        }
                        else{
                            mistakes = Integer.parseInt(msj.substring(msj.indexOf("?")+1).trim());
                            word = msj.substring(msj.indexOf("*")+1, msj.indexOf("?")-1);
                            
                            
                            printBody(mistakes);
                            System.out.println("PALABRA / ORACIÓN: "+word);
                            System.out.println("Selecciona una letra\n");
                        }
                        
                        k.interestOps(SelectionKey.OP_WRITE);
                        continue;
                    }//if
                }
                
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static void printVictory(String word){
        
        System.out.println("  -----");
        System.out.println(" |     |");
        System.out.println(" |");
        System.out.println(" |");
        System.out.println(" |      (uwu)");
        System.out.println(" |       \\|/");
        System.out.println("- -      / \\ ");
        System.out.println("G A N A S T E");
        System.out.println("PALABRA / ORACIÓN CORRECTA: " + word);
    }
    
    public static void printBody( int num ){
        
        System.out.println("\nVidas restantes: "+(5-num));
        if( num == 1 ){
            System.out.println("  -----");
            System.out.println(" |     |");
            System.out.println(" |");
            System.out.println(" |");
            System.out.println(" |");
            System.out.println(" |");
            System.out.println("- -");
        }else if( num == 2){
            System.out.println("  -----");
            System.out.println(" |     |");
            System.out.println(" |   (._.)");
            System.out.println(" |");
            System.out.println(" |");
            System.out.println(" |");
            System.out.println("- -");
        }else if( num == 3){
            System.out.println("  -----");
            System.out.println(" |     |");
            System.out.println(" |   (._.)");
            System.out.println(" |     |");
            System.out.println(" |");
            System.out.println(" |");
            System.out.println("- -");
        }else if( num == 4 ){
            System.out.println("  -----");
            System.out.println(" |     |");
            System.out.println(" |   (.o.)");
            System.out.println(" |    /|\\ ");
            System.out.println(" |");
            System.out.println(" |");
            System.out.println("- -");
        }else if( num == 5){
            System.out.println("  -----");
            System.out.println(" |     |");
            System.out.println(" |   (x_x)");
            System.out.println(" |    /|\\ ");
            System.out.println(" |    / \\ ");
            System.out.println(" |");
            System.out.println("- -");
            System.out.println("P E R D I S T E");
        }
    }
}
