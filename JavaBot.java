package helpbot;

import java.io.*;
import java.net.*;
import java.util.regex.*;

/**
 *
 * @author Josh
 */
public class JavaBot extends Thread
{
    Socket socket;
    String nick;
    String channel;
    
    Factoid pre;
    
    /**
     * JavaBot constructor. Takes arguments and builds a bot object from them.
     * 
     * @param server
     * @param port
     * @param nick
     * @param channel
     */
    JavaBot( String server, int port, String nick, String channel )
    {
        try {
            this.socket         = new Socket( server, port );
            this.nick           = nick;
            this.channel        = channel;
            this.pre = new Factoid( "factoids/pre.fact" );
            this.start();
            this.homeostasis();
            System.out.println( "Successfully connected to: " + server );
        } catch( IOException e ) {
            System.err.println( "There was an error connecting to the server" );
        }
    }
    
    /**
     * read data from the InputStream
     * 
     * @return
     */
    private String read()
    {
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader( this.socket.getInputStream() ) );
            return br.readLine();
        } catch( IOException e ) {
            System.err.println( "There was an error reading from the server" );
            return null;
        }
    }
    
    /**
     * Write to the OutputStream
     * 
     * @param message
     */
    private void write( String message )
    {
        try {
            BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter( socket.getOutputStream() ) );
            bw.write( message + "\n" );
            bw.flush();
        } catch( IOException e ) {
            System.err.println( "There was an error writing to the server" );
        }
    }
    
    private String getChannel( String line )
    {
        if( line.indexOf( "#" ) != -1 && line.lastIndexOf( ":" ) != -1 && line.indexOf( "PRIVMSG" ) != -1  && line.indexOf( "#" ) < line.lastIndexOf( " :" ) )
            return line.substring( line.indexOf( "#" ), line.lastIndexOf( " :" ) ).replaceAll( " ", "" );
        else if( line.indexOf( "PRIVMSG" ) != -1 )
            return line.substring( 1, line.indexOf( "!" ) );
        else return null;
    }

    private String getNick( String line )
    {
        return line.substring( 1, line.indexOf( "!" ) );
    }

    private String getMessage( String line )
    {
        if( line.lastIndexOf( ":" ) != -1 )
            return line.substring( line.lastIndexOf( " :" ) + 2 );
        else return null;
    }
    
    /*
     * RFC 1459 implementation
     */
    
    /**
     * replying to PINGs from the IRC server
     */
    private void pong()
    {
        this.write( "PONG " + this.channel );
    }
    
    /**
     * Supply password for connections
     * @param pass
     */
    private void pass( String pass )
    {
        this.write( "PASS " + pass );
    }
    
    /**
     * Supply nickname for connections
     * @param nick
     */
    private void nick( String nick )
    {
        this.write( "NICK " + nick );
    }
    
    /**
     * Supply user information for connections
     * @param nick
     * @param host
     * @param server
     * @param name
     */
    private void user( String nick, String host, String server, String name )
    {
        this.write( "USER " + nick + " " + host + " " + server + " " + name );
    }
    
    /**
     * Joiin a given channel
     * @param channel
     */
    private void join( String channel )
    {
        this.write( "JOIN " + channel );
    }
    
    /**
     * Part rfom a given channel
     * @param channel
     */
    private void part( String channel )
    {
        this.write( "PART " + channel );
    }
    
    /**
     * Quit from current IRC session
     * @param message
     */
    private void quit( String message )
    {
        this.write( "QUIT " + message );
        try {
            this.socket.close();
            System.out.println( "Successfully disconnected from IRC" );
        } catch( IOException e ) {
            System.err.println( "There was an error disconnecting from the server" );
        }
    }
    
    /**
     * Communications link to the current channel in IRC
     * @param message
     */
    private void message( String message )
    {
        this.write( "PRIVMSG " + this.channel + " :" + message );
    }
    
    
    /*
     * Regular expression patterns
     */
    Pattern pingRegex = Pattern.compile( "^PING" );
    Pattern exitRegex = Pattern.compile( "^!exit" );
    
    
    /**
     * System for keeping the bot "alive" and making it able to interact with IRC
     */
    private void homeostasis()
    {
       this.nick( this.nick );
       this.user( this.nick, "enigmagroup.org", "thenullbyte.org", this.nick );
       this.join( this.channel );
       System.out.println( "Successfully connected to IRC" );
       this.message( "I am here to help!" );
       
       String currLine = null;
       while( ( currLine = read() ) != null )
       {
           System.out.println( currLine );
           
           Matcher ping = pingRegex.matcher( this.getMessage( currLine ) );
           if( ping.find() )
               this.pong();
           
           Matcher exit = exitRegex.matcher( this.getMessage( currLine ) );
           if( exit.find() )
               this.quit( "Peace Niggaz" );
           
           //---------- added functionality ----------//
           
           if( this.getMessage( currLine ).startsWith( "!pre" ) )
           {
               String[] query = this.getMessage( currLine ).split( " " );
               this.message( this.pre.query( query[1] ) );
           }
       }
    }
}
