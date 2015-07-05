package hello;
import org.bff.javampd.*;
import org.bff.javampd.objects.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class MPDWrapper {

	private static final Logger logger = LogManager.getLogger();

	private static final int portNumber = 6001;
	private static final String password = "password";


	private static MPD mpdglobal = null;

	private MPDWrapper(){
		try {
            mpdglobal = new MPD.Builder()
                        .port(portNumber)
                        .password(password)
                        .build();
        }
        catch (Exception e) {
        	logger.error("MPD connection error. Is MPD running?");
        }
    }

    public static MPD getMPD()
    {
      	if (mpdglobal == null){
        	// it's ok, we can call this constructor
          	MPDWrapper wrapper = new MPDWrapper();
        }
        return mpdglobal;
    }
}
