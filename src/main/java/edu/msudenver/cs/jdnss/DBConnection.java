package edu.msudenver.cs.jdnss;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ObjectMessage;

import java.sql.*;
import java.util.*;

class DBConnection {
    private Connection conn;
   
    private static final Logger logger = JDNSS.logger;

    // com.mysql.jdbc.Driver
    // jdbc:mysql://localhost/JDNSS
    DBConnection(final String dbClass, final String dbURL, final String dbUser,
                 final String dbPass) {
        String user = dbUser == null ? "" : dbUser;
        String pass = dbPass == null ? "" : dbPass;

        // load up the class
        try {
            Class.forName(dbClass);
        } catch (ClassNotFoundException cnfe) {
            logger.catching(cnfe);
        }

        try {
            conn = DriverManager.getConnection(dbURL, user, pass);
        } catch (SQLException sqle) {
            logger.catching(sqle);
            assert false;
        }
 
    }
    

	DBZone getZone(final String name) throws JDNSEXception  {
		DBZone retval = new DBZone(name, 1, this); 
		ResultSet rs = null;
		Statement stmt = null;
		try {
			logger.traceEntry(new ObjectMessage(name));
			Set<String> v = new HashSet<>(); 

			stmt = conn.createStatement();
			// first, get them all 

			logger.traceEntry("\"SELECT * FROM domains  =={}", name);
			rs = stmt.executeQuery("SELECT * FROM domains;");

			while (rs.next()) {
				String nameTmp = rs.getString("name");
				logger.traceEntry("_SELECT * FROM domains {}==>{}", name, nameTmp);
				v.add(nameTmp);
			}
			
			logger.traceEntry("r=={}", v);

			if (v.size() == 0) {
				logger.traceEntry("return new DBZone(__0___)");
				return new DBZone();
			}

			// then, find the longest that matches
			String s = null;

			s = Utils.findLongest(v, name);
			logger.trace(s);

			// then, populate a DBZone with what we found.

			logger.traceEntry("SELECT * FROM domains WHERE name = '{}..'", s);
			rs = stmt.executeQuery("SELECT * FROM domains WHERE name = '" + s + "'");

			rs.next();
			final int domainId = rs.getInt("id");
			logger.trace("domainId={}", domainId);

			assert !rs.next();

			logger.traceExit(s);
			retval =  new DBZone(s, domainId, this);

		} catch (SQLException e) {
			logger.error("return new DBZone(name,1,this);");
			throw  new JDNSEXception (e.getMessage());
			 
		} catch (JDNSEXception e) {
			throw e;
		}finally {
			stclose(stmt);
			rsclose(rs); 
		}
		return retval;
	}

	

 

	private void stclose(Statement stmt) {
		try {
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private void rsclose(ResultSet rs) {
		try {
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    public List<RR> get(final RRCode type, final String name, final int domainId) {
        logger.traceEntry("_T:"+new ObjectMessage(type));
        logger.traceEntry("_N:"+new ObjectMessage(name));
        logger.traceEntry("_D:"+new ObjectMessage(domainId));
        Statement stmt = null;
        ResultSet rs =null;
        List<RR> ret= new ArrayList<RR>();
        try { 
            String stype = type.name();
            logger.trace(stype);
             
            stmt = conn.createStatement();
            // SQL injection over DNS! 0:-O
            rs = stmt.executeQuery(
                    "SELECT * FROM records where domain_id = " + domainId +
                            " AND name = \"" + name + "\"" +
                            " AND type = \"" + stype + "\"");
            logger.trace(
                    "SELECT * FROM records where domain_id = " + domainId +
                    " AND name = \"" + name + "\"" +
                    " AND type = \"" + stype + "\";");
            while (rs.next()) {
            	RR addRR = addRR(type, name, rs);
            	logger.trace("+{}",addRR);
				ret.add( addRR ) ; 
            }
        } catch (SQLException sqle) {
            logger.catching(sqle);
        } catch (Exception e) {
            logger.catching(e);
        }finally {
			stclose(stmt);
			rsclose(rs); 
			logger.trace("RET:{}",ret);
		}
        return ret;
 
    }

    private RR addRR(final RRCode type, final String name, final ResultSet rs) throws SQLException {
        final String dbname = rs.getString("name");
        final String dbcontent = rs.getString("content");
        final int dbttl = rs.getInt("ttl");
        final int dbprio = rs.getInt("prio");
        logger.trace("RR:{}//{}//{}//{}//[{}]/:{}",dbname,dbcontent,dbttl,dbprio,type,name);
        final RR emptyRR = new EmptyRR();

        switch (type) {
            case SOA: {
                String[] s = dbcontent.split("\\s+");
                return new SOARR(dbname, s[0], s[1],
                        Integer.parseInt(s[2]), Integer.parseInt(s[3]),
                        Integer.parseInt(s[4]), Integer.parseInt(s[5]),
                        Integer.parseInt(s[6]), dbttl);
            }
            case NS: { return new NSRR(dbname, dbttl, dbcontent); }
            case A: { return new ARR(dbname, dbttl, dbcontent); }
            case AAAA: { return new AAAARR(dbname, dbttl, dbcontent); }
            case MX: { return new MXRR(dbname, dbttl, dbcontent, dbprio); }
            case TXT: { return new TXTRR(dbname, dbttl, dbcontent); }
            case CNAME: { return new CNAMERR(dbname, dbttl, dbcontent); }
            case PTR: { return new PTRRR(dbname, dbttl, dbcontent); }
            case HINFO: {
                final String[] s = dbcontent.split("\\s+");
                return new HINFORR(dbname, dbttl, s[0], s[1]);
            }
            case RRSIG:
            case NSEC:
            case DNSKEY:
            case NSEC3:
            case NSEC3PARAM: { return emptyRR; }
            default: {
                logger.warn("requested type " + type + " for " + name + " not found");
                break;
            }
        }
        return emptyRR;
    }
}
