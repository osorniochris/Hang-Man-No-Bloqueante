package ahorcadonobloqueante;

import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Random;

/**
 *
 * @author chistopher
 */
public class Servidor {
    
    public static String easyWords [];
    public static String mediumWords [];
    public static String impossibleWords [];
    
    public static int pto = 9600;

    public static void main(String[] args) {
        
        try{
            
            initializeEW();
            initializeMW();
            initializeIW();
            
            ServerSocketChannel s = ServerSocketChannel.open();
            s.configureBlocking(false);
            s.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            s.socket().bind(new InetSocketAddress(pto));
            
            Selector sel = Selector.open();
            s.register(sel, SelectionKey.OP_ACCEPT);
            System.out.println("Servidor iniciado, esperando clientes...");
            
            while(true){
                
                sel.select();
                
                Iterator <SelectionKey> it = sel.selectedKeys().iterator();
                
                while(it.hasNext()){
                   
                    SelectionKey k = (SelectionKey)it.next();
                    it.remove();
                    
                    if( k.isAcceptable() ){
                        
                        SocketChannel cl = s.accept();
                        System.out.println("Cliente conectado desde "+cl.socket().getInetAddress().getHostAddress()+":"+cl.socket().getPort()+"\n");
                        cl.configureBlocking(false);
                        cl.register(sel, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        continue;
                        
                    }
                    else if( k.isReadable() ){
                        
                        SocketChannel ch =(SocketChannel)k.channel();
                        ByteBuffer b = ByteBuffer.allocate(2000);
                        b.clear();
                        
                        String word = "", line = "", aux="";
                        Random rand = new Random();
                        int pos, mistakes = 0;
                        
                        int n = ch.read(b);
                        b.flip();
                        String msj = new String(b.array(), 0, n);
                        
                        if( msj.contains("<begin>") ){
                            
                            int difficulty = Integer.parseInt(msj.substring(msj.indexOf("*")+1, msj.indexOf("%")));
                            mistakes = 0;
                            pos = rand.nextInt(20);

                            if( difficulty == 1 ){
                                //easy
                                word = easyWords[pos];
                                System.out.println("Dificultad : Fácil");
                            }
                            else if( difficulty == 2 ){
                                //easy
                                word = mediumWords[pos];
                                System.out.println("Dificultad : Media");
                            } 
                            else if( difficulty == 3 ){
                                //easy
                                word = impossibleWords[pos];
                                System.out.println("Dificultad : Imposible");
                            } 

                            System.out.println("Palabra : " + word);

                            //line is compared with word, aux just an auxiliar to print
                            line = wToLine(word,0);
                            aux = wToLine(line,3);

                            System.out.println("Progreso : "+ aux);
                            String envio=difficulty+"/"+pos+"*"+aux+"?"+"0";
                            
                            ByteBuffer b2=ByteBuffer.wrap(envio.getBytes());
                            ch.write(b2);
                            continue;
                            
                        }
                        else{
                            
                            String character = "";
                            
                            int dif = Integer.parseInt(Character.toString(msj.charAt(0))); 
                            int w = Integer.parseInt(msj.substring(msj.indexOf("/")+1, msj.indexOf("*")));
                            line = msj.substring(msj.indexOf("%")+1, msj.indexOf("#"));
                            mistakes = Integer.parseInt(msj.substring(msj.indexOf("#")+1).trim());
                            msj = msj.substring(msj.indexOf("*")+1, msj.indexOf("%"));
                            
                            character = msj.toUpperCase();
                            
                            if( dif == 1){
                                //line is compared with word, aux just an auxiliar to print
                                word = easyWords[w];
                                line = wToLine(line, 4);
                            }
                            else if( dif == 2){
                                //line is compared with word, aux just an auxiliar to print
                                word = mediumWords[w];
                                line = wToLine(line, 4);
                            }
                            else if (dif == 3){
                                //line is compared with word, aux just an auxiliar to print
                                word = impossibleWords[w];
                                line = wToLine(line, 5);
                            }
                            
                            
                            if( character.equals("-1") ){
                                //cliente tries to type more than one char

                                System.out.println("Más de una letra");
                                line = replaceCharInLine(word, line, character);
                                aux = wToLine(line,3);
                                System.out.println("Progreso : "+ aux);

                                String envio=dif+"/"+w+"*"+aux+"?"+mistakes;
                                ByteBuffer b2=ByteBuffer.wrap(envio.getBytes());
                                ch.write(b2);
                                continue;
                            }
                            else{
                                if( word.contains(character)){
                                    //check the char received and update line
                                    line = replaceCharInLine(word, line, character);
                                    aux = wToLine(line,3);
                                    System.out.println("Progreso : "+ aux);
                                    String envio = "";

                                    if( line.equals(word) ){

                                        envio=dif+"/"+w+"*"+aux+"?"+mistakes+"!end|win";
                                        System.out.println("V I C T O R I A");
                                        ByteBuffer b2=ByteBuffer.wrap(envio.getBytes());
                                        ch.write(b2);
                                        ch.close();
                                        continue;
                                        
                                    }
                                    else{
                                        envio=dif+"/"+w+"*"+aux+"?"+mistakes;
                                        ByteBuffer b2=ByteBuffer.wrap(envio.getBytes());
                                        ch.write(b2);
                                        continue;
                                    }

                                }
                                else{
                                    
                                    aux = wToLine(word,3);
                                    //the typed char in not in the word
                                    mistakes = mistakes + 1;
                                    String envio=aux+"?"+mistakes;
                                    if( mistakes == 5){

                                        envio=dif+"/"+w+"*"+aux+"?"+mistakes+"!end"+"|fail";
                                        System.out.println("D E R R O T A");
                                        ByteBuffer b2=ByteBuffer.wrap(envio.getBytes());
                                        ch.write(b2);
                                        ch.close();
                                        continue;
                                    }
                                    else{
                                        aux = wToLine(line,3);
                                        System.out.println("Progreso : "+ aux);

                                        envio=dif+"/"+w+"*"+aux+"?"+mistakes;
                                        ByteBuffer b2=ByteBuffer.wrap(envio.getBytes());
                                        ch.write(b2);
                                        continue;
                                    }
                                }
                            }
                            
                            //continue;
                        }//else
                        
                    }// isReadable
                    
                }
                
            }
            
        }catch(Exception e){
            e.printStackTrace();
        }
        
    }
    
    private static String wToLine(String word, int spaces){
        String aux = "";
        
        if( spaces == 1 ){
            aux = word.replaceAll("\\s", "    "); 
            aux = aux.replaceAll("[A-Z]", "_ ");
        }
        else if( spaces == 0 ){
            aux = word.replaceAll("[A-Z]", "_");
        }
        else if( spaces == 2 ){
            aux = word.replaceAll("\\s", "    "); 
        }
        else if( spaces == 3 ){
            for (int i = 0; i < word.length(); i++) {
                aux = aux + word.charAt(i) + " ";
            }
        }
        else if( spaces == 4 ){
            aux = word.replaceAll("\\s", ""); 
        }
        else if( spaces == 5 ){
            aux = word.replaceAll(" ", "*");
            aux = aux.replace("**", " ");
            aux = aux.replace("*", "");
        }
        return aux;
    }
    
    private static String replaceCharInLine(String word, String line, String l){
        String aux = "";
        for (int i = 0; i < word.length(); i++) {
            if( word.charAt(i) == l.charAt(0)){
                aux = aux +l.charAt(0);
            }
            else {
                aux = aux + line.charAt(i);
            }
            
        }
        
        return aux;
    }
    
    private static void initializeEW(){
        
        //less than threee syllables 
        
        easyWords = new String [20];
        
        easyWords [0] = "MUJER";
        easyWords [1] = "HOMBRE";
        easyWords [2] = "GENTE";
        easyWords [3] = "AMOR";
        easyWords [4] = "CARA";
        easyWords [5] = "PIE";
        easyWords [6] = "BRAZO";
        easyWords [7] = "CODO";
        easyWords [8] = "ALMA";
        easyWords [9] = "HIJO";
        easyWords [10] = "VIDA";
        easyWords [11] = "PLAYA";
        easyWords [12] = "GATO";
        easyWords [13] = "PERRO";
        easyWords [14] = "FRUTA";
        easyWords [15] = "CARNE";
        easyWords [16] = "FECHA";
        easyWords [17] = "MES";
        easyWords [18] = "LUNES";
        easyWords [19] = "NORTE";
    }
    
    private static void initializeMW(){
        
        //more then 3 syllables
        
        mediumWords = new String [20];
        
        mediumWords [0] = "CALABAZA";
        mediumWords [1] = "CONSUMIDOR";
        mediumWords [2] = "HERRAMIENTAS";
        mediumWords [3] = "COMENTARIOS";
        mediumWords [4] = "PARLAMENTO";
        mediumWords [5] = "COMPUTADORA";
        mediumWords [6] = "NOMENCLATURA";
        mediumWords [7] = "DESMANTELAMIENTO";
        mediumWords [8] = "LICENCIATURA";
        mediumWords [9] = "INTELIGENCIA";
        mediumWords [10] = "EXPERIENCIA";
        mediumWords [11] = "REGRIGERADOR";
        mediumWords [12] = "AFICIONADO";
        mediumWords [13] = "CAPITALISMO";
        mediumWords [14] = "ORDENADOR";
        mediumWords [15] = "SECADORA";
        mediumWords [16] = "CONGELADOR";
        mediumWords [17] = "COMPLICADO";
        mediumWords [18] = "LIMPIEZA";
        mediumWords [19] = "VENTILADOR";
    }
    
    private static void initializeIW(){
        
        //sentences
        
        impossibleWords = new String [20];

        impossibleWords [0] = "CAMARON QUE SE DUERME SE LO LLEVA LA CORRIENTE";
        impossibleWords [1] = "AGUA PASA POR MI CASA, CATE DE MI CORAZON";
        impossibleWords [2] = "POR ANDAR VIENDO LA LUNA ME CAI EN LA LAGUNA";
        impossibleWords [3] = "ANITA LAVA LA TINA";
        impossibleWords [4] = "AL QUE MADRUGA DIOS LO AYUDA";
        impossibleWords [5] = "DE TAL PALO TAL ASTILLA";
        impossibleWords [6] = "QUIEN CON LOBOS SE JUNTA, A AULLAR SE ENSEÑA";
        impossibleWords [7] = "HIERBA MALA NUNCA MUERE";
        impossibleWords [8] = "EL QUE MUCHO ABARCA POCO APRIETA";
        impossibleWords [9] = "AL MAL TIEMPO, BUENA CARA";
        impossibleWords [10] = "AUNQUE LA MONA SE VISTA DE SEDA, MONA SE QUEDA";
        impossibleWords [11] = "BARRIGA LLENA CORAZON CONTENTO";
        impossibleWords [12] = "CUIDADO CON EL PERRO";
        impossibleWords [13] = "LA CURIOSIDAD MATO AL GATO";
        impossibleWords [14] = "DIME CON QUIEN ANDAS Y TE DIRE QUIEN ERES";
        impossibleWords [15] = "EL QUE RIE AL ULTIMO RIE MEJOR";
        impossibleWords [16] = "EL QUE PARTE Y REPARTE SE QUEDA CON LA MEJOR PARTE";
        impossibleWords [17] = "MAS SABE EL DIABLO POR VIEJO QUE POR DIABLO";
        impossibleWords [18] = "NO TODO LO QUE BRILLA ES ORO";
        impossibleWords [19] = "OJOS QUE NO VEN, CORAZON QUE NO SIENTE";
    }
    
}
