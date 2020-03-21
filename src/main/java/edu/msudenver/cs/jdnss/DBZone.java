
package edu.msudenver.cs.jdnss;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ObjectMessage;

import java.util.ArrayList;
import java.util.List;

class DBZone extends Zone {
    private static final Logger logger = JDNSS.logger;
    private final String zoneName;
	private int domainId;
	private DBConnection dbConnection;

    DBZone() { zoneName = null; }

    @Override
    boolean isEmpty() { return zoneName == null; }

    // com.mysql.jdbc.Driver
    // jdbc:mysql://localhost/JDNSS
    DBZone(final String zoneName, final int domainId, final DBConnection dbConnection)
    {
        this.zoneName = zoneName;
        this.domainId = domainId;
        this.dbConnection = dbConnection;
    }

    public String getName() { return zoneName; }

    public List<RR> get(final RRCode type, final String name)
    {
        logger.traceEntry(new ObjectMessage(type));
        logger.traceEntry(new ObjectMessage(name));

        return dbConnection.get(type, name, domainId);
        
    }
}