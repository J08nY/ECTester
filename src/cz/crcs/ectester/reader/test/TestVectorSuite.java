package cz.crcs.ectester.reader.test;

import cz.crcs.ectester.applet.ECTesterApplet;
import cz.crcs.ectester.applet.EC_Consts;
import cz.crcs.ectester.data.EC_Store;
import cz.crcs.ectester.reader.CardMngr;
import cz.crcs.ectester.reader.ECTester;
import cz.crcs.ectester.reader.Util;
import cz.crcs.ectester.reader.command.Command;
import cz.crcs.ectester.reader.ec.*;
import cz.crcs.ectester.reader.response.Response;
import javacard.security.KeyPair;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Jan Jancar johny@neuromancer.sk
 */
public class TestVectorSuite extends TestSuite {

    public TestVectorSuite(EC_Store dataStore, ECTester.Config cfg) {
        super(dataStore, cfg, "test", "The test-vectors suite contains a collection of test vectors which test basic ECDH correctness.");
    }

    @Override
    public void setup(CardMngr cardManager) throws IOException {
        /* Set original curves (secg/nist/brainpool). Set keypairs from test vectors.
         * Do ECDH both ways, export and verify that the result is correct.
         */
        Map<String, EC_KAResult> results = dataStore.getObjects(EC_KAResult.class, "test");
        for (EC_KAResult result : results.values()) {
            EC_Curve curve = dataStore.getObject(EC_Curve.class, result.getCurve());
            if (cfg.namedCurve != null && !(result.getCurve().startsWith(cfg.namedCurve) || result.getCurve().equals(cfg.namedCurve))) {
                continue;
            }
            if (curve.getBits() != cfg.bits && !cfg.all) {
                continue;
            }
            if (curve.getField() == KeyPair.ALG_EC_FP && !cfg.primeField || curve.getField() == KeyPair.ALG_EC_F2M && !cfg.binaryField) {
                continue;
            }
            EC_Params onekey = dataStore.getObject(EC_Keypair.class, result.getOneKey());
            if (onekey == null) {
                onekey = dataStore.getObject(EC_Key.Private.class, result.getOneKey());
            }
            EC_Params otherkey = dataStore.getObject(EC_Keypair.class, result.getOtherKey());
            if (otherkey == null) {
                otherkey = dataStore.getObject(EC_Key.Public.class, result.getOtherKey());
            }
            if (onekey == null || otherkey == null) {
                throw new IOException("Test vector keys couldn't be located.");
            }
            List<Test> testVector = new LinkedList<>();

            testVector.add(new Test.Simple(new Command.Allocate(cardManager, ECTesterApplet.KEYPAIR_BOTH, curve.getBits(), curve.getField()), Result.Value.SUCCESS));
            testVector.add(new Test.Simple(new Command.Set(cardManager, ECTesterApplet.KEYPAIR_BOTH, EC_Consts.CURVE_external, curve.getParams(), curve.flatten()), Result.Value.SUCCESS));
            //tests.add(new Test.Simple(new Command.Generate(cardManager, ECTesterApplet.KEYPAIR_BOTH), Result.Value.SUCCESS));
            testVector.add(new Test.Simple(new Command.Set(cardManager, ECTesterApplet.KEYPAIR_LOCAL, EC_Consts.CURVE_external, EC_Consts.PARAMETER_S, onekey.flatten(EC_Consts.PARAMETER_S)), Result.Value.SUCCESS));
            testVector.add(new Test.Simple(new Command.Set(cardManager, ECTesterApplet.KEYPAIR_REMOTE, EC_Consts.CURVE_external, EC_Consts.PARAMETER_W, otherkey.flatten(EC_Consts.PARAMETER_W)), Result.Value.SUCCESS));
            testVector.add(new Test.Simple(new Command.ECDH(cardManager, ECTesterApplet.KEYPAIR_REMOTE, ECTesterApplet.KEYPAIR_LOCAL, ECTesterApplet.EXPORT_TRUE, EC_Consts.CORRUPTION_NONE, result.getKA()), (command, response) -> {
                Response.ECDH dh = (Response.ECDH) response;
                if (!dh.successful())
                    return new Result(Result.Value.FAILURE, "ECDH was unsuccessful.");
                if (!dh.hasSecret())
                    return new Result(Result.Value.FAILURE, "ECDH response did not contain the derived secret.");
                if (!Util.compareBytes(dh.getSecret(), 0, result.getParam(0), 0, dh.secretLength())) {
                    int firstDiff = Util.diffBytes(dh.getSecret(), 0, result.getParam(0), 0, dh.secretLength());
                    return new Result(Result.Value.FAILURE, "ECDH derived secret does not match the test, first difference was at byte " + String.valueOf(firstDiff) + ".");
                }
                return new Result(Result.Value.SUCCESS);
            }));
            tests.add(Test.Compound.all(Result.Value.SUCCESS, "Test vector " + result.getId(), testVector.toArray(new Test[0])));
            tests.add(new Test.Simple(new Command.Cleanup(cardManager), Result.Value.ANY));

        }
    }
}