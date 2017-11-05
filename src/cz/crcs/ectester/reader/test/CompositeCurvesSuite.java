package cz.crcs.ectester.reader.test;

import cz.crcs.ectester.applet.ECTesterApplet;
import cz.crcs.ectester.applet.EC_Consts;
import cz.crcs.ectester.data.EC_Store;
import cz.crcs.ectester.reader.CardMngr;
import cz.crcs.ectester.reader.ECTester;
import cz.crcs.ectester.reader.command.Command;
import cz.crcs.ectester.reader.ec.EC_Curve;
import cz.crcs.ectester.reader.ec.EC_Key;
import javacard.security.KeyPair;

import java.util.Map;

import static cz.crcs.ectester.reader.test.Result.ExpectedValue;

/**
 * @author Jan Jancar johny@neuromancer.sk
 */
public class CompositeCurvesSuite extends TestSuite {

    public CompositeCurvesSuite(EC_Store dataStore, ECTester.Config cfg) {
        super(dataStore, cfg, "composite", "The composite suite tests ECDH over curves with composite order. This should generally fail, as using such a curve is unsafe.");
    }

    @Override
    public void setup(CardMngr cardManager) {
        /* Do the default tests with the public keys set to provided smallorder keys
         * over composite order curves. Essentially small subgroup attacks.
         * These should fail, the curves aren't safe so that if the computation with
         * a small order public key succeeds the private key modulo the public key order
         * is revealed.
         */
        Map<String, EC_Key> keys = dataStore.getObjects(EC_Key.class, "composite");
        for (EC_Key key : keys.values()) {
            EC_Curve curve = dataStore.getObject(EC_Curve.class, key.getCurve());
            if (cfg.namedCurve != null && !(key.getCurve().startsWith(cfg.namedCurve) || key.getCurve().equals(cfg.namedCurve))) {
                continue;
            }
            if (curve.getField() == KeyPair.ALG_EC_FP && !cfg.primeField || curve.getField() == KeyPair.ALG_EC_F2M && !cfg.binaryField) {
                continue;
            }
            if ((curve.getBits() == cfg.bits || cfg.all)) {
                tests.add(new Test.Simple(new Command.Allocate(cardManager, ECTesterApplet.KEYPAIR_BOTH, curve.getBits(), curve.getField()), ExpectedValue.SUCCESS));
                tests.add(new Test.Simple(new Command.Set(cardManager, ECTesterApplet.KEYPAIR_BOTH, EC_Consts.CURVE_external, curve.getParams(), curve.flatten()), ExpectedValue.ANY));
                tests.add(new Test.Simple(new Command.Generate(cardManager, ECTesterApplet.KEYPAIR_LOCAL), ExpectedValue.ANY));
                Command ecdhCommand = new Command.ECDH_direct(cardManager, ECTesterApplet.KEYPAIR_LOCAL, ECTesterApplet.EXPORT_FALSE, EC_Consts.CORRUPTION_NONE, EC_Consts.KA_ECDH, key.flatten());
                tests.add(new Test.Simple(ecdhCommand, ExpectedValue.FAILURE, "Card correctly rejected to do ECDH over a composite order curve.", "Card incorrectly does ECDH over a composite order curve, leaks bits of private key."));
                tests.add(new Test.Simple(new Command.Cleanup(cardManager), ExpectedValue.ANY));
            }
        }
    }
}
