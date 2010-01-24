package helpbot;

import java.io.*;
import java.util.*;

/**
 * Simple parser for InfoBot factoid files
 * TODO: recursive loading of "factoids" dir
 * 
 * This is actually a really crappy way of doing this, generating the database
 * from a flatfile at runtime, but it was the quickest thing I could think of.
 * Sue me.
 * 
 * @author dydx
 */
public class Factoid
{
    private static Map database;
    
    /**
     * Generate a small database of facts to be searchable by users in IRC
     * 
     * This is actually a main feature of my bot, and I am pondering making
     * a recursive include for the .fact files, so that I could just enter
     * the location (directory) of several files and have it compile them
     * all into the database.
     * 
     * Or maybe create a new instance of Factoids for every subject area?
     * Factoids sports = new Factoids( "sports.fact" );
     * Factoids movies = new Factoids( "movies.fact" );
     * etc etc etc
     * 
     * then just create new regex based string searching to differentiate
     * 
     * @param file
     */
    Factoid( String file )
    {
        try {
            String root = System.getProperty( "user.dir" );//get docroot
            String filename = root  + File.separator + file;
            database = getDatabase( filename );//get our database!
        } catch( Exception e ) {
            System.err.println( "Please check the path to the .fact file to make sure it is correct." );
        }
    }
    
    /**
     * Create a BufferedReader from given filepath
     * 
     * @param filepath
     * @return
     */
    private static BufferedReader getReader( String filepath )
    {
        filepath = filepath.replace( '/', File.separatorChar );
        try {
            BufferedReader reader = new BufferedReader(
                    new FileReader( filepath) );
            return reader;
        } catch( FileNotFoundException e ) {
            System.err.println( "File Not Found at: " + filepath );
            return null;
        }
    }
    
    /**
     * Parse .fact files and create miniature database(HashMap) from it
     * 
     * @param reader
     * @return
     */
    private static Map getDatabase( String filepath )
    {
        try {
            BufferedReader reader = getReader( filepath );//create this on a more local level
            Map<String, String> _database = new HashMap<String, String>();//our local database
            
            String line;
            while( ( line = reader.readLine() ) != null )
            {
                String[] data = line.split( " => " );// .fact files are "key => value"
                _database.put( data[0], data[1] );//populate the database
            }
            return _database;//this will be referenced over to the main database
        } catch( NullPointerException e ) {
            //3 hours of recoding to try to figure out why i was getting a NullPointerException.
            System.err.println( "Check fact file for extraneous whitespace or newlines" );
            //Turns out it was an issue with the .fact file i was using to test with
            return null;
        } catch( IOException e ) {
            System.err.println( "There was an issue generating the database" );
            return null;
        }
    }
    
    /**
     * public accessible method for querying the database
     * 
     * @param search
     * @return
     */
    public String query( String search )
    {
        String recordset = (String)database.get( search );
        if( recordset != null )
            return search + ": " + recordset;
        else
            return "Search Term \""+ search + "\" Not Found";
    }

}
