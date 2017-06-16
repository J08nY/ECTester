package cz.crcs.ectester.reader.ec;

import cz.crcs.ectester.applet.EC_Consts;

/**
 * @author Jan Jancar johny@neuromancer.sk
 */
public class EC_Keypair extends EC_Params {
    private String curve;
    private String desc;

    public EC_Keypair(String curve) {
        super(EC_Consts.PARAMETERS_KEYPAIR);
        this.curve = curve;
    }

    public EC_Keypair(String curve, String desc) {
        this(curve);
        this.desc = desc;
    }

    public String getCurve() {
        return curve;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public String toString() {
        return "<" + getId() + "> EC Keypair, over " + curve + (desc == null ? "" : ": " + desc);
    }
}