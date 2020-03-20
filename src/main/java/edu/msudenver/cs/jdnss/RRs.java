package edu.msudenver.cs.jdnss;

import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

class RRs {
    private int location;
    private final byte[] buffer;
    private static final Logger logger = JDNSS.logger;
    private final int numQuestions;
    private final int numAnswers;
    private final int numAuthorities;
    private final int numAdditionals;

    private final RR[] questions;
    private final RR[] answers;
    private final RR[] authorities;
    private final RR[] additionals;

    RRs(byte buffer[], int numQuestions, int numAnswers,
               int numAuthorities, int numAdditionals) {
        this.buffer = Arrays.copyOf(buffer, buffer.length);
        this.numQuestions = numQuestions;
        this.numAnswers = numAnswers;
        this.numAuthorities = numAuthorities;
        this.numAdditionals = numAdditionals;

        questions = new RR[numQuestions];
        answers = new RR[numAnswers];
        authorities = new RR[numAuthorities];
        additionals = new RR[numAdditionals];

        parseQuestions();
    }

    private void parseQuestions() {
        logger.traceEntry();

        /*
        The question section is used to carry the "question" in most queries,
        i.e., the parameters that deinfo what is being asked.  The section
        contains QDCOUNT(usually 1) entries, each of the following format:

        1  1  1  1  1  1
        0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
        |                                               |
        /                     QNAME                     /
        /                                               /
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
        |                     QTYPE                     |
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
        |                     QCLASS                    |
        +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
        */

        for (int i = 0; i < numQuestions; i++) {
            Map.Entry<String, Integer> StringAndNumber = null;

            try {
                StringAndNumber = Utils.parseName(location, buffer);
            } catch (AssertionError ae) {
                questions[i] = null;
                assert false;
            }

            location = StringAndNumber.getValue();
            int qtype = Utils.addThem(buffer[location], buffer[location + 1]);
            location += 2;
            // FIXME: QU/QM
            // logger.fatal(buffer[location] & 0x80);
            // int qclass = Utils.addThem(buffer[location], buffer[location + 1]);
            location += 2;

            questions[i] = new QRR(StringAndNumber.getKey(),
                    RRCode.findCode(qtype));
        }
    }

    private String display(String title, RR rrs[]) {
        String s = title + "\n";

        for (int i = 0; i < rrs.length; i++) {
            // put a newline on all except the last
            s += rrs[i] + (i < rrs.length - 1 ? "\n" : "");
        }

        return s;
    }

    public String toString() {
        String s = "";

        if (numQuestions > 0) {
            s += display("Questions:", questions);
        }
        if (numAnswers > 0) {
            s += display("Answers:", answers);
        }
        if (numAuthorities > 0) {
            s += display("Authorities:", authorities);
        }
        if (numAdditionals > 0) {
            s += display("Additional:", additionals);
        }

        return s;
    }
}



























class EmptyRR extends RR {
    EmptyRR() {
        super(null, null, -1);
    }

    @Override
    boolean isEmpty() {
        return true;
    }

    @Override
    protected byte[] getBytes() {
        assert false;
        return new byte[0];
    }
}

/**
 * Just a simple class for queries.
 */
class QRR extends RR {
    QRR(final String name, final RRCode type) {
        super(name, type, 0);
    }

    @Override
    protected byte[] getBytes() {
        return new byte[0];
    }
}

class SOARR extends RR {
    private final String domain;
    private final String server;
    private final String contact;
    private final int serial;
    private final int refresh;
    private final int retry;
    private final int expire;
    private final int minimum;

    SOARR(final String domain, final String server, final String contact, final int serial, final int refresh, final int retry, final int expire, final int minimum, int ttl) {
        super(domain, RRCode.SOA, ttl);
        this.domain = domain;
        this.server = server;
        this.contact = contact;
        this.serial = serial;
        this.refresh = refresh;
        this.retry = retry;
        this.expire = expire;
        this.minimum = minimum;
    }

    /*
    ** 1035:
    ** "... SOA records are always distributed with a zero
    ** TTL to prohibit caching."
    **
    ** 2182:
    ** It may be observed that in section 3.2.1 of RFC1035, which defines
    ** the format of a Resource Record, that the definition of the TTL field
    ** contains a throw away line which states that the TTL of an SOA record
    ** should always be sent as zero to prevent caching.  This is mentioned
    ** nowhere else, and has not generally been implemented.
    ** Implementations should not assume that SOA records will have a TTL of
    ** zero, nor are they required to send SOA records with a TTL of zero.
    **
    ** this however does not say what SHOULD be sent as the TTL...
    */
    public int getMinimum() {
        return minimum;
    }

    @Override
    protected byte[] getBytes() {
        byte[] a = Utils.convertString(server);
        a = Utils.combine(a, Utils.convertString(contact));
        a = Utils.combine(a, Utils.getBytes(serial));
        a = Utils.combine(a, Utils.getBytes(refresh));
        a = Utils.combine(a, Utils.getBytes(retry));
        a = Utils.combine(a, Utils.getBytes(expire));
        a = Utils.combine(a, Utils.getBytes(minimum));
        return a;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public java.lang.String toString() {
        return "SOARR(domain=" + this.domain + ", server=" + this.server + ", contact=" + this.contact + ", serial=" + this.serial + ", refresh=" + this.refresh + ", retry=" + this.retry + ", expire=" + this.expire + ", minimum=" + this.getMinimum() + ")";
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof SOARR)) return false;
        final SOARR other = (SOARR) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        if (!super.equals(o)) return false;
        final java.lang.Object this$domain = this.domain;
        final java.lang.Object other$domain = other.domain;
        if (this$domain == null ? other$domain != null : !this$domain.equals(other$domain)) return false;
        final java.lang.Object this$server = this.server;
        final java.lang.Object other$server = other.server;
        if (this$server == null ? other$server != null : !this$server.equals(other$server)) return false;
        final java.lang.Object this$contact = this.contact;
        final java.lang.Object other$contact = other.contact;
        if (this$contact == null ? other$contact != null : !this$contact.equals(other$contact)) return false;
        if (this.serial != other.serial) return false;
        if (this.refresh != other.refresh) return false;
        if (this.retry != other.retry) return false;
        if (this.expire != other.expire) return false;
        if (this.getMinimum() != other.getMinimum()) return false;
        return true;
    }

    @java.lang.SuppressWarnings("all")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof SOARR;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = super.hashCode();
        final java.lang.Object $domain = this.domain;
        result = result * PRIME + ($domain == null ? 43 : $domain.hashCode());
        final java.lang.Object $server = this.server;
        result = result * PRIME + ($server == null ? 43 : $server.hashCode());
        final java.lang.Object $contact = this.contact;
        result = result * PRIME + ($contact == null ? 43 : $contact.hashCode());
        result = result * PRIME + this.serial;
        result = result * PRIME + this.refresh;
        result = result * PRIME + this.retry;
        result = result * PRIME + this.expire;
        result = result * PRIME + this.getMinimum();
        return result;
    }
}

class HINFORR extends RR {
    private final String CPU;
    private final String OS;

    HINFORR(final String name, final int ttl, final String CPU, final String OS) {
        super(name, RRCode.HINFO, ttl);
        this.CPU = CPU;
        this.OS = OS;
    }

    @Override
    protected byte[] getBytes() {
        return Utils.combine(Utils.toCS(CPU), Utils.toCS(OS));
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public java.lang.String toString() {
        return "HINFORR(CPU=" + this.CPU + ", OS=" + this.OS + ")";
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof HINFORR)) return false;
        final HINFORR other = (HINFORR) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        if (!super.equals(o)) return false;
        final java.lang.Object this$CPU = this.CPU;
        final java.lang.Object other$CPU = other.CPU;
        if (this$CPU == null ? other$CPU != null : !this$CPU.equals(other$CPU)) return false;
        final java.lang.Object this$OS = this.OS;
        final java.lang.Object other$OS = other.OS;
        if (this$OS == null ? other$OS != null : !this$OS.equals(other$OS)) return false;
        return true;
    }

    @java.lang.SuppressWarnings("all")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof HINFORR;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = super.hashCode();
        final java.lang.Object $CPU = this.CPU;
        result = result * PRIME + ($CPU == null ? 43 : $CPU.hashCode());
        final java.lang.Object $OS = this.OS;
        result = result * PRIME + ($OS == null ? 43 : $OS.hashCode());
        return result;
    }
}

class MXRR extends RR {
    private final String host;
    private final int preference;

    MXRR(final String name, final int ttl, final String host, final int preference) {
        super(name, RRCode.MX, ttl);
        this.host = host;
        this.preference = preference;
    }

    @Override
    protected byte[] getBytes() {
        byte[] c = new byte[2];
        c[0] = Utils.getByte(preference, 2);
        c[1] = Utils.getByte(preference, 1);
        return Utils.combine(c, Utils.convertString(host));
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public java.lang.String toString() {
        return "MXRR(host=" + this.getHost() + ", preference=" + this.getPreference() + ")";
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof MXRR)) return false;
        final MXRR other = (MXRR) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        if (!super.equals(o)) return false;
        final java.lang.Object this$host = this.getHost();
        final java.lang.Object other$host = other.getHost();
        if (this$host == null ? other$host != null : !this$host.equals(other$host)) return false;
        if (this.getPreference() != other.getPreference()) return false;
        return true;
    }

    @java.lang.SuppressWarnings("all")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof MXRR;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = super.hashCode();
        final java.lang.Object $host = this.getHost();
        result = result * PRIME + ($host == null ? 43 : $host.hashCode());
        result = result * PRIME + this.getPreference();
        return result;
    }

    @java.lang.SuppressWarnings("all")
    public String getHost() {
        return this.host;
    }

    @java.lang.SuppressWarnings("all")
    public int getPreference() {
        return this.preference;
    }
}

abstract class STRINGRR extends RR {
    String string;

    STRINGRR(final String name, final RRCode type, int ttl) {
        super(name, type, ttl);
    }

    @Override
    protected byte[] getBytes() {
        return Utils.convertString(string);
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public java.lang.String toString() {
        return "STRINGRR(string=" + this.getString() + ")";
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof STRINGRR)) return false;
        final STRINGRR other = (STRINGRR) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        if (!super.equals(o)) return false;
        final java.lang.Object this$string = this.getString();
        final java.lang.Object other$string = other.getString();
        if (this$string == null ? other$string != null : !this$string.equals(other$string)) return false;
        return true;
    }

    @java.lang.SuppressWarnings("all")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof STRINGRR;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = super.hashCode();
        final java.lang.Object $string = this.getString();
        result = result * PRIME + ($string == null ? 43 : $string.hashCode());
        return result;
    }

    @java.lang.SuppressWarnings("all")
    public String getString() {
        return this.string;
    }
}

class TXTRR extends STRINGRR {
    TXTRR(final String name, final int ttl, final String text) {
        super(name, RRCode.TXT, ttl);
        this.string = text;
    }

    @Override
    protected byte[] getBytes() {
        return Utils.toCS(string);
    }
}

class NSRR extends STRINGRR {
    NSRR(final String domain, final int ttl, final String nameserver) {
        super(domain, RRCode.NS, ttl);
        this.string = nameserver;
    }

    @Override
    public String getString() {
        return string;
    }
}

class CNAMERR extends STRINGRR {
    CNAMERR(final String alias, final int ttl, final String canonical) {
        super(alias, RRCode.CNAME, ttl);
        this.string = canonical;
    }
}

class PTRRR extends STRINGRR {
    PTRRR(final String address, final int ttl, final String host) {
        super(address, RRCode.PTR, ttl);
        this.string = host;
    }
}

abstract class ADDRRR extends RR {
    protected String address;

    ADDRRR(final String name, final RRCode type, final int ttl) {
        super(name, type, ttl);
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public java.lang.String toString() {
        return "ADDRRR(super=" + super.toString() + ", address=" + this.address + ")";
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof ADDRRR)) return false;
        final ADDRRR other = (ADDRRR) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        if (!super.equals(o)) return false;
        final java.lang.Object this$address = this.address;
        final java.lang.Object other$address = other.address;
        if (this$address == null ? other$address != null : !this$address.equals(other$address)) return false;
        return true;
    }

    @java.lang.SuppressWarnings("all")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof ADDRRR;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = super.hashCode();
        final java.lang.Object $address = this.address;
        result = result * PRIME + ($address == null ? 43 : $address.hashCode());
        return result;
    }
}

class ARR extends ADDRRR {
    ARR(final String name, final int ttl, final String address) {
        super(name, RRCode.A, ttl);
        this.address = address;
    }

    @Override
    protected byte[] getBytes() {
        return Utils.IPV4(address);
    }
}

class AAAARR extends ADDRRR {
    AAAARR(final String name, final int ttl, final String address) {
        super(name, RRCode.AAAA, ttl);
        this.address = address;
    }

    @Override
    protected byte[] getBytes() {
        return Utils.IPV6(address);
    }
}

class DNSKEYRR extends RR {
    private final int flags;
    private final int protocol;
    private final int algorithm;
    private final String publicKey;

    DNSKEYRR(final String domain, final int ttl, final int flags, final int protocol, final int algorithm, final String publicKey) {
        super(domain, RRCode.DNSKEY, ttl);
        this.flags = Integer.parseUnsignedInt(String.valueOf(flags));
        this.protocol = Integer.parseUnsignedInt(String.valueOf(protocol));
        this.algorithm = Integer.parseUnsignedInt(String.valueOf(algorithm));
        this.publicKey = publicKey;
    }

    @Override
    protected byte[] getBytes() {
        byte[] a = new byte[0];
        a = Utils.combine(a, Utils.getTwoBytes(flags, 2));
        a = Utils.combine(a, Utils.getByte(protocol, 1));
        a = Utils.combine(a, Utils.getByte(algorithm, 1));
        try {
            a = Utils.combine(a, Base64.getDecoder().decode(publicKey.getBytes()));
        } catch (Exception e) {
            assert false;
        }
        return a;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public java.lang.String toString() {
        return "DNSKEYRR(flags=" + this.flags + ", protocol=" + this.protocol + ", algorithm=" + this.algorithm + ", publicKey=" + this.publicKey + ")";
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof DNSKEYRR)) return false;
        final DNSKEYRR other = (DNSKEYRR) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        if (!super.equals(o)) return false;
        if (this.flags != other.flags) return false;
        if (this.protocol != other.protocol) return false;
        if (this.algorithm != other.algorithm) return false;
        final java.lang.Object this$publicKey = this.publicKey;
        final java.lang.Object other$publicKey = other.publicKey;
        if (this$publicKey == null ? other$publicKey != null : !this$publicKey.equals(other$publicKey)) return false;
        return true;
    }

    @java.lang.SuppressWarnings("all")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof DNSKEYRR;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = super.hashCode();
        result = result * PRIME + this.flags;
        result = result * PRIME + this.protocol;
        result = result * PRIME + this.algorithm;
        final java.lang.Object $publicKey = this.publicKey;
        result = result * PRIME + ($publicKey == null ? 43 : $publicKey.hashCode());
        return result;
    }
}
// https://www.iana.org/assignments/dns-sec-alg-numbers/dns-sec-alg-numbers.xhtml
class NSEC3RR extends RR {
    private final int hashAlgorithm;
    private final int flags;
    private final int iterations;
    private final String salt;
    private final String nextHashedOwnerName;
    private final Set<RRCode> types;

    NSEC3RR(final String domain, final int ttl, final int hashAlgorithm, final int flags, final int iterations, final String salt, final String nextHashedOwnerName, final Set<RRCode> types) {
        super(domain, RRCode.NSEC3, ttl);
        this.hashAlgorithm = hashAlgorithm;
        this.flags = flags;
        this.iterations = iterations;
        this.salt = salt;
        this.nextHashedOwnerName = nextHashedOwnerName;
        this.types = types;
    }

    @Override
    protected byte[] getBytes() {
        byte[] a = new byte[0];
        a = Utils.combine(a, Utils.getByte(hashAlgorithm, 1));
        a = Utils.combine(a, Utils.getByte(flags, 2));
        a = Utils.combine(a, Utils.getTwoBytes(iterations, 1));
        a = Utils.combine(a, Utils.getByte(salt.length(), 1));
        a = Utils.combine(a, Utils.convertString(salt));
        a = Utils.combine(a, Utils.getByte(this.nextHashedOwnerName.length(), 1));
        a = Utils.combine(a, Utils.convertString(nextHashedOwnerName));
        assert false;
        return a;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public java.lang.String toString() {
        return "NSEC3RR(hashAlgorithm=" + this.hashAlgorithm + ", flags=" + this.flags + ", iterations=" + this.iterations + ", salt=" + this.salt + ", nextHashedOwnerName=" + this.nextHashedOwnerName + ", types=" + this.types + ")";
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof NSEC3RR)) return false;
        final NSEC3RR other = (NSEC3RR) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        if (!super.equals(o)) return false;
        if (this.hashAlgorithm != other.hashAlgorithm) return false;
        if (this.flags != other.flags) return false;
        if (this.iterations != other.iterations) return false;
        final java.lang.Object this$salt = this.salt;
        final java.lang.Object other$salt = other.salt;
        if (this$salt == null ? other$salt != null : !this$salt.equals(other$salt)) return false;
        final java.lang.Object this$nextHashedOwnerName = this.nextHashedOwnerName;
        final java.lang.Object other$nextHashedOwnerName = other.nextHashedOwnerName;
        if (this$nextHashedOwnerName == null ? other$nextHashedOwnerName != null : !this$nextHashedOwnerName.equals(other$nextHashedOwnerName)) return false;
        final java.lang.Object this$types = this.types;
        final java.lang.Object other$types = other.types;
        if (this$types == null ? other$types != null : !this$types.equals(other$types)) return false;
        return true;
    }

    @java.lang.SuppressWarnings("all")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof NSEC3RR;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = super.hashCode();
        result = result * PRIME + this.hashAlgorithm;
        result = result * PRIME + this.flags;
        result = result * PRIME + this.iterations;
        final java.lang.Object $salt = this.salt;
        result = result * PRIME + ($salt == null ? 43 : $salt.hashCode());
        final java.lang.Object $nextHashedOwnerName = this.nextHashedOwnerName;
        result = result * PRIME + ($nextHashedOwnerName == null ? 43 : $nextHashedOwnerName.hashCode());
        final java.lang.Object $types = this.types;
        result = result * PRIME + ($types == null ? 43 : $types.hashCode());
        return result;
    }
}

class NSEC3PARAMRR extends RR {
    private final int hashAlgorithm;
    private final int flags;
    private final int iterations;
    private final String salt;

    NSEC3PARAMRR(final String domain, final int ttl, final int hashAlgorithm, final int flags, final int iterations, final String salt) {
        super(domain, RRCode.NSEC3PARAM, ttl);
        this.hashAlgorithm = hashAlgorithm;
        this.flags = flags;
        this.iterations = iterations;
        this.salt = salt;
    }

    @Override
    protected byte[] getBytes() {
        assert false;
        return null;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public java.lang.String toString() {
        return "NSEC3PARAMRR(hashAlgorithm=" + this.hashAlgorithm + ", flags=" + this.flags + ", iterations=" + this.iterations + ", salt=" + this.salt + ")";
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof NSEC3PARAMRR)) return false;
        final NSEC3PARAMRR other = (NSEC3PARAMRR) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        if (!super.equals(o)) return false;
        if (this.hashAlgorithm != other.hashAlgorithm) return false;
        if (this.flags != other.flags) return false;
        if (this.iterations != other.iterations) return false;
        final java.lang.Object this$salt = this.salt;
        final java.lang.Object other$salt = other.salt;
        if (this$salt == null ? other$salt != null : !this$salt.equals(other$salt)) return false;
        return true;
    }

    @java.lang.SuppressWarnings("all")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof NSEC3PARAMRR;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = super.hashCode();
        result = result * PRIME + this.hashAlgorithm;
        result = result * PRIME + this.flags;
        result = result * PRIME + this.iterations;
        final java.lang.Object $salt = this.salt;
        result = result * PRIME + ($salt == null ? 43 : $salt.hashCode());
        return result;
    }
}

class RRSIG extends RR {
    private final RRCode typeCovered;
    private final int algorithm;
    private final int labels;
    private final int originalttl;
    private final int signatureExpiration; //32 bit unsigned
    private final int signatureInception; // 32 bit unsigned
    private final int keyTag;
    private final String signersName;
    private final String signature;

    RRSIG(final String domain, final int ttl, final RRCode typeCovered, final int algorithm, final int labels, final int originalttl, final int expiration, final int inception, final int keyTag, final String signersName, final String signature) {
        super(domain, RRCode.RRSIG, ttl);
        this.typeCovered = typeCovered;
        this.algorithm = algorithm;
        this.labels = labels;
        this.originalttl = originalttl;
        this.signatureExpiration = expiration;
        this.signatureInception = inception;
        this.keyTag = keyTag;
        this.signersName = signersName;
        this.signature = signature;
    }

    @Override
    protected byte[] getBytes() {
        byte[] a = new byte[0];
        a = Utils.combine(a, Utils.getTwoBytes(typeCovered.getCode(), 2));
        a = Utils.combine(a, Utils.getByte(algorithm, 1));
        a = Utils.combine(a, Utils.getByte(labels, 1));
        a = Utils.combine(a, Utils.getBytes(originalttl));
        a = Utils.combine(a, Utils.getTwoBytes(signatureExpiration, 4));
        a = Utils.combine(a, Utils.getTwoBytes(signatureExpiration, 2));
        a = Utils.combine(a, Utils.getTwoBytes(signatureInception, 4));
        a = Utils.combine(a, Utils.getTwoBytes(signatureInception, 2));
        a = Utils.combine(a, Utils.getTwoBytes(keyTag, 2));
        a = Utils.combine(a, Utils.convertString(signersName));
        try {
            a = Utils.combine(a, Base64.getDecoder().decode(signature.getBytes()));
        } catch (Exception e) {
            assert false;
        }
        return a;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public java.lang.String toString() {
        return "RRSIG(typeCovered=" + this.getTypeCovered() + ", algorithm=" + this.algorithm + ", labels=" + this.labels + ", originalttl=" + this.originalttl + ", signatureExpiration=" + this.signatureExpiration + ", signatureInception=" + this.signatureInception + ", keyTag=" + this.keyTag + ", signersName=" + this.signersName + ", signature=" + this.signature + ")";
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof RRSIG)) return false;
        final RRSIG other = (RRSIG) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        if (!super.equals(o)) return false;
        final java.lang.Object this$typeCovered = this.getTypeCovered();
        final java.lang.Object other$typeCovered = other.getTypeCovered();
        if (this$typeCovered == null ? other$typeCovered != null : !this$typeCovered.equals(other$typeCovered)) return false;
        if (this.algorithm != other.algorithm) return false;
        if (this.labels != other.labels) return false;
        if (this.originalttl != other.originalttl) return false;
        if (this.signatureExpiration != other.signatureExpiration) return false;
        if (this.signatureInception != other.signatureInception) return false;
        if (this.keyTag != other.keyTag) return false;
        final java.lang.Object this$signersName = this.signersName;
        final java.lang.Object other$signersName = other.signersName;
        if (this$signersName == null ? other$signersName != null : !this$signersName.equals(other$signersName)) return false;
        final java.lang.Object this$signature = this.signature;
        final java.lang.Object other$signature = other.signature;
        if (this$signature == null ? other$signature != null : !this$signature.equals(other$signature)) return false;
        return true;
    }

    @java.lang.SuppressWarnings("all")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof RRSIG;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = super.hashCode();
        final java.lang.Object $typeCovered = this.getTypeCovered();
        result = result * PRIME + ($typeCovered == null ? 43 : $typeCovered.hashCode());
        result = result * PRIME + this.algorithm;
        result = result * PRIME + this.labels;
        result = result * PRIME + this.originalttl;
        result = result * PRIME + this.signatureExpiration;
        result = result * PRIME + this.signatureInception;
        result = result * PRIME + this.keyTag;
        final java.lang.Object $signersName = this.signersName;
        result = result * PRIME + ($signersName == null ? 43 : $signersName.hashCode());
        final java.lang.Object $signature = this.signature;
        result = result * PRIME + ($signature == null ? 43 : $signature.hashCode());
        return result;
    }

    @java.lang.SuppressWarnings("all")
    public RRCode getTypeCovered() {
        return this.typeCovered;
    }
}

class NSECRR extends RR {
    private final String nextDomainName;
    private final Set<RRCode> resourceRecords; //map more appopriate <RRCode, RR> ??

    NSECRR(final String domain, final int ttl, final String nextDomainName, final Set<RRCode> resourceRecords) {
        super(domain, RRCode.NSEC, ttl);
        this.nextDomainName = nextDomainName;
        this.resourceRecords = resourceRecords;
    }

    @Override
    protected byte[] getBytes() {
        byte[] a = new byte[0];
        a = Utils.combine(a, Utils.convertString(nextDomainName));
        a = Utils.combine(a, buildBitMap());
        return a;
    }

    private byte[] buildBitMap() {
        int largestRcode = 0;
        for (RRCode rr : resourceRecords) {
            if (rr.getCode() > largestRcode) {
                largestRcode = rr.getCode();
            }
        }
        int length = (largestRcode + 8) / 8;
        byte[] bitMap = new byte[length];
        byte[] a = {0};
        a = Utils.combine(a, (byte) length);
        a = Utils.combine(a, setBits(bitMap));
        return a;
    }

    private byte[] setBits(byte[] bitMap) {
        for (RRCode rr : resourceRecords) {
            switch (rr) {
            case A: 
                bitMap[0] = (byte) (bitMap[0] + 64);
                break;
            case NS: 
                bitMap[0] = (byte) (bitMap[0] + 32);
                break;
            case CNAME: 
                bitMap[0] = (byte) (bitMap[0] + 4);
                break;
            case SOA: 
                bitMap[0] = (byte) (bitMap[0] + 2);
                break;
            case PTR: 
                bitMap[1] = (byte) (bitMap[1] + 8);
                break;
            case HINFO: 
                bitMap[1] = (byte) (bitMap[1] + 4);
                break;
            case MX: 
                bitMap[1] = (byte) (bitMap[1] + 1);
                break;
            case TXT: 
                bitMap[2] = (byte) (bitMap[2] + 128);
                break;
            case AAAA: 
                bitMap[3] = (byte) (bitMap[3] + 8);
                break;
            case A6: 
                bitMap[4] = (byte) (bitMap[4] + 2);
                break;
            case DNAME: 
                bitMap[4] = (byte) (bitMap[4] + 1);
                break;
            case DS: 
                bitMap[5] = (byte) (bitMap[5] + 16);
                break;
            case RRSIG: 
                bitMap[5] = (byte) (bitMap[5] + 2);
                break;
            case NSEC: 
                bitMap[5] = (byte) (bitMap[5] + 1);
                break;
            case DNSKEY: 
                bitMap[6] = (byte) (bitMap[6] + 128);
                break;
            case NSEC3: 
                bitMap[6] = (byte) (bitMap[6] + 32);
                break;
            case NSEC3PARAM: 
                bitMap[6] = (byte) (bitMap[6] + 16);
                break;
            default: 
                logger.error("Couldn\'t add/find " + rr + " to NSEC bit map");
                break;
            }
        }
        return bitMap;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public java.lang.String toString() {
        return "NSECRR(nextDomainName=" + this.nextDomainName + ", resourceRecords=" + this.resourceRecords + ")";
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof NSECRR)) return false;
        final NSECRR other = (NSECRR) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        if (!super.equals(o)) return false;
        final java.lang.Object this$nextDomainName = this.nextDomainName;
        final java.lang.Object other$nextDomainName = other.nextDomainName;
        if (this$nextDomainName == null ? other$nextDomainName != null : !this$nextDomainName.equals(other$nextDomainName)) return false;
        final java.lang.Object this$resourceRecords = this.resourceRecords;
        final java.lang.Object other$resourceRecords = other.resourceRecords;
        if (this$resourceRecords == null ? other$resourceRecords != null : !this$resourceRecords.equals(other$resourceRecords)) return false;
        return true;
    }

    @java.lang.SuppressWarnings("all")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof NSECRR;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = super.hashCode();
        final java.lang.Object $nextDomainName = this.nextDomainName;
        result = result * PRIME + ($nextDomainName == null ? 43 : $nextDomainName.hashCode());
        final java.lang.Object $resourceRecords = this.resourceRecords;
        result = result * PRIME + ($resourceRecords == null ? 43 : $resourceRecords.hashCode());
        return result;
    }
}
