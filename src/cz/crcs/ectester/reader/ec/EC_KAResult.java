package cz.crcs.ectester.reader.ec;

import cz.crcs.ectester.reader.Util;

/**
 * @author Jan Jancar johny@neuromancer.sk
 */
public class EC_KAResult extends EC_Data {

    private byte ka;
    private String curve;
    private String oneKey;
    private String otherKey;

    private String desc;

    public EC_KAResult(byte ka, String curve, String oneKey, String otherKey) {
        super(1);
        this.ka = ka;
        this.curve = curve;
        this.oneKey = oneKey;
        this.otherKey = otherKey;
    }

    public EC_KAResult(String id, byte ka, String curve, String oneKey, String otherKey) {
        this(ka, curve, oneKey, otherKey);
        this.id = id;
    }

    public EC_KAResult(String id, byte ka, String curve, String oneKey, String otherKey, String desc) {
        this(id, ka, curve, oneKey, otherKey);
        this.desc = desc;
    }

    public byte getKA() {
        return ka;
    }

    public String getCurve() {
        return curve;
    }

    public String getOneKey() {
        return oneKey;
    }

    public String getOtherKey() {
        return otherKey;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public String toString() {
        String algo = Util.getKA(ka);
        return "<" + getId() + "> " + algo + " result over " + curve + ", " + oneKey + " + " + otherKey + (desc == null ? "" : ": " + desc);
    }

}