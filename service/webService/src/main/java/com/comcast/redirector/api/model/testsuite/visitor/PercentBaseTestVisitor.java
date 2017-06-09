/**
 * Copyright 2017 Comcast Cable Communications Management, LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Alexander Binkovsky (abinkovski@productengine.com)
 */

package com.comcast.redirector.api.model.testsuite.visitor;

import com.comcast.redirector.api.model.IVisitable;
import com.comcast.redirector.api.model.Value;
import com.comcast.redirector.api.model.testsuite.Parameter;
import com.comcast.redirector.common.RedirectorConstants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class PercentBaseTestVisitor<T extends IVisitable> extends BaseTestSuiteExpressionVisitor<T> {
    private static final Map<Integer,String> percentToAccountId = new HashMap<>();
    static {
        percentToAccountId.put(0, "8808eda0dd3dec4e4df50499f2fc75e8");
        percentToAccountId.put(1, "af21d0c97db2e27e13572cbf59eb343d");
        percentToAccountId.put(2, "a9078e8653368c9c291ae2f8b74012e7");
        percentToAccountId.put(3, "81e5f81db77c596492e6f1a5a792ed53");
        percentToAccountId.put(4, "e3251075554389fe91d17a794861d47b");
        percentToAccountId.put(5, "17fafe5f6ce2f1904eb09d2e80a4cbf6");
        percentToAccountId.put(6, "a223c6b3710f85df22e9377d6c4f7553");
        percentToAccountId.put(7, "0966289037ad9846c5e994be2a91bafa");
        percentToAccountId.put(8, "0e4e946668cf2afc4299b462b812caca");
        percentToAccountId.put(9, "59f51fd6937412b7e56ded1ea2470c25");
        percentToAccountId.put(10, "489d0396e6826eb0c1e611d82ca8b215");
        percentToAccountId.put(11, "411ae1bf081d1674ca6091f8c59a266f");
        percentToAccountId.put(12, "db6ebd0566994d14a1767f14eb6fba81");
        percentToAccountId.put(13, "b197ffdef2ddc3308584dce7afa3661b");
        percentToAccountId.put(14, "8fb21ee7a2207526da55a679f0332de2");
        percentToAccountId.put(15, "e94f63f579e05cb49c05c2d050ead9c0");
        percentToAccountId.put(16, "01d8bae291b1e4724443375634ccfa0e");
        percentToAccountId.put(17, "07042ac7d03d3b9911a00da43ce0079a");
        percentToAccountId.put(18, "8b0d268963dd0cfb808aac48a549829f");
        percentToAccountId.put(19, "daa96d9681a21445772454cbddf0cac1");
        percentToAccountId.put(20, "ab541d874c7bc19ab77642849e02b89f");
        percentToAccountId.put(21, "55c567fd4395ecef6d936cf77b8d5b2b");
        percentToAccountId.put(22, "ced556cd9f9c0c8315cfbe0744a3baf0");
        percentToAccountId.put(23, "cfa5301358b9fcbe7aa45b1ceea088c6");
        percentToAccountId.put(24, "49b8b4f95f02e055801da3b4f58e28b7");
        percentToAccountId.put(25, "4dcf435435894a4d0972046fc566af76");
        percentToAccountId.put(26, "ebd6d2f5d60ff9afaeda1a81fc53e2d0");
        percentToAccountId.put(27, "0f3d014eead934bbdbacb62a01dc4831");
        percentToAccountId.put(28, "2b3bf3eee2475e03885a110e9acaab61");
        percentToAccountId.put(29, "490640b43519c77281cb2f8471e61a71");
        percentToAccountId.put(30, "42d6c7d61481d1c21bd1635f59edae05");
        percentToAccountId.put(31, "5607fe8879e4fd269e88387e8cb30b7e");
        percentToAccountId.put(32, "db1915052d15f7815c8b88e879465a1e");
        percentToAccountId.put(33, "253f7b5d921338af34da817c00f42753");
        percentToAccountId.put(34, "e60e81c4cbe5171cd654662d9887aec2");
        percentToAccountId.put(35, "99adff456950dd9629a5260c4de21858");
        percentToAccountId.put(36, "08040837089cdf46631a10aca5258e16");
        percentToAccountId.put(37, "35309226eb45ec366ca86a4329a2b7c3");
        percentToAccountId.put(38, "d1a69640d53a32a9fb13e93d1c8f3104");
        percentToAccountId.put(39, "cda72177eba360ff16b7f836e2754370");
        percentToAccountId.put(40, "1373b284bc381890049e92d324f56de0");
        percentToAccountId.put(41, "c88d8d0a6097754525e02c2246d8d27f");
        percentToAccountId.put(42, "4e6cd95227cb0c280e99a195be5f6615");
        percentToAccountId.put(43, "351b33587c5fdd93bd42ef7ac9995a28");
        percentToAccountId.put(44, "18ead4c77c3f40dabf9735432ac9d97a");
        percentToAccountId.put(45, "3a20f62a0af1aa152670bab3c602feed");
        percentToAccountId.put(46, "4d6e4749289c4ec58c0063a90deb3964");
        percentToAccountId.put(47, "facf9f743b083008a894eee7baa16469");
        percentToAccountId.put(48, "be53ee61104935234b174e62a07e53cf");
        percentToAccountId.put(49, "18d10dc6e666eab6de9215ae5b3d54df");
        percentToAccountId.put(50, "dfa92d8f817e5b08fcaafb50d03763cf");
        percentToAccountId.put(51, "655ea4bd3b5736d88afc30c9212ccddf");
        percentToAccountId.put(52, "7bd28f15a49d5e5848d6ec70e584e625");
        percentToAccountId.put(53, "452bf208bf901322968557227b8f6efe");
        percentToAccountId.put(54, "0d4f4805c36dc6853edfa4c7e1638b48");
        percentToAccountId.put(55, "ed4227734ed75d343320b6a5fd16ce57");
        percentToAccountId.put(56, "88a199611ac2b85bd3f76e8ee7e55650");
        percentToAccountId.put(57, "b710915795b9e9c02cf10d6d2bdb688c");
        percentToAccountId.put(58, "277281aada22045c03945dcb2ca6f2ec");
        percentToAccountId.put(59, "49af6c4e558a7569d80eee2e035e2bd7");
        percentToAccountId.put(60, "894b77f805bd94d292574c38c5d628d5");
        percentToAccountId.put(61, "d72fbbccd9fe64c3a14f85d225a046f4");
        percentToAccountId.put(62, "0ebcc77dc72360d0eb8e9504c78d38bd");
        percentToAccountId.put(63, "309fee4e541e51de2e41f21bebb342aa");
        percentToAccountId.put(64, "87ec2f451208df97228105657edb717f");
        percentToAccountId.put(65, "9c19a2aa1d84e04b0bd4bc888792bd1e");
        percentToAccountId.put(66, "83e8ef518174e1eb6be4a0778d050c9d");
        percentToAccountId.put(67, "9e984c108157cea74c894b5cf34efc44");
        percentToAccountId.put(68, "c559da2ba967eb820766939a658022c8");
        percentToAccountId.put(69, "1bc0249a6412ef49b07fe6f62e6dc8de");
        percentToAccountId.put(70, "f3173935ed8ac4bf073c1bcd63171f8a");
        percentToAccountId.put(71, "a368b0de8b91cfb3f91892fbf1ebd4b2");
        percentToAccountId.put(72, "495dabfd0ca768a3c3abd672079f48b6");
        percentToAccountId.put(73, "9597353e41e6957b5e7aa79214fcb256");
        percentToAccountId.put(74, "faafda66202d234463057972460c04f5");
        percentToAccountId.put(75, "a2cc63e065705fe938a4dda49092966f");
        percentToAccountId.put(76, "a14ac55a4f27472c5d894ec1c3c743d2");
        percentToAccountId.put(77, "7a6a74cbe87bc60030a4bd041dd47b78");
        percentToAccountId.put(78, "52947e0ade57a09e4a1386d08f17b656");
        percentToAccountId.put(79, "69d658d0b2859e32cd4dc3b970c8496c");
        percentToAccountId.put(80, "e9fd7c2c6623306db59b6aef5c0d5cac");
        percentToAccountId.put(81, "71a58e8cb75904f24cde464161c3e766");
        percentToAccountId.put(82, "962e56a8a0b0420d87272a682bfd1e53");
        percentToAccountId.put(83, "f60bb6bb4c96d4df93c51bd69dcc15a0");
        percentToAccountId.put(84, "d1dc3a8270a6f9394f88847d7f0050cf");
        percentToAccountId.put(85, "4462bf0ddbe0d0da40e1e828ebebeb11");
        percentToAccountId.put(86, "c7af0926b294e47e52e46cfebe173f20");
        percentToAccountId.put(87, "fc528592c3858f90196fbfacc814f235");
        percentToAccountId.put(88, "9327969053c0068dd9e07c529866b94d");
        percentToAccountId.put(89, "a981f2b708044d6fb4a71a1463242520");
        percentToAccountId.put(90, "bc573864331a9e42e4511de6f678aa83");
        percentToAccountId.put(91, "97d0145823aeb8ed80617be62e08bdcc");
        percentToAccountId.put(92, "efb76cff97aaf057654ef2f38cd77d73");
        percentToAccountId.put(93, "5c50b4df4b176845cd235b6a510c6903");
        percentToAccountId.put(94, "46031b3d04dc90994ca317a7c55c4289");
        percentToAccountId.put(95, "6ba3af5d7b2790e73f0de32e5c8c1798");
        percentToAccountId.put(96, "0771fc6f0f4b1d7d1bb73bbbe14e0e31");
        percentToAccountId.put(97, "21fe5b8ba755eeaece7a450849876228");
        percentToAccountId.put(98, "42a3964579017f3cb42b26605b9ae8ef");
        percentToAccountId.put(99, "1c54985e4f95b7819ca0357c0cb9a09f");
        percentToAccountId.put(100, "52c5189391854c93e8a0e1326e56c14f");
    }

    @Override
    public void visit(T item) {
        Parameter parameter = new Parameter();
        parameter.setName(RedirectorConstants.Parameters.ACCOUNT_ID);
        parameter.setValues(
            Collections.singletonList(
                new Value(percentToAccountId.get(getPercentValue(item)))));

        addParameter(parameter);
    }

    abstract protected Integer getPercentValue(T item);
}
