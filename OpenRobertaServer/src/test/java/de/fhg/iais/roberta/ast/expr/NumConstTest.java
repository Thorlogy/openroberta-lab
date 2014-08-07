package de.fhg.iais.roberta.ast.expr;

import org.junit.Assert;
import org.junit.Test;

import de.fhg.iais.roberta.ast.syntax.expr.Assoc;
import de.fhg.iais.roberta.ast.syntax.expr.NumConst;
import de.fhg.iais.roberta.ast.transformer.JaxbTransformer;
import de.fhg.iais.roberta.helper.Helper;

public class NumConstTest {

    @Test
    public void make() throws Exception {
        String a = "BlockAST [project=[[NumConst [0]]]]";

        Assert.assertEquals(a, Helper.generateASTString("/ast/math/math_num_constant.xml"));
    }

    @Test
    public void getValue() throws Exception {
        JaxbTransformer transformer = Helper.generateAST("/ast/math/math_num_constant.xml");

        NumConst numConst = (NumConst) transformer.getTree().get(0);

        Assert.assertEquals("0", numConst.getValue());
    }

    @Test
    public void getPresedance() throws Exception {
        JaxbTransformer transformer = Helper.generateAST("/ast/math/math_num_constant.xml");

        NumConst numConst = (NumConst) transformer.getTree().get(0);

        Assert.assertEquals(999, numConst.getPrecedence());
    }

    @Test
    public void getAssoc() throws Exception {
        JaxbTransformer transformer = Helper.generateAST("/ast/math/math_num_constant.xml");

        NumConst numConst = (NumConst) transformer.getTree().get(0);

        Assert.assertEquals(Assoc.NONE, numConst.getAssoc());
    }
}
