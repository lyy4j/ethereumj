/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.sharding.service;

import org.ethereum.listener.EthereumListener;
import org.ethereum.sharding.ShardingTestHelper;
import org.ethereum.util.blockchain.StandaloneBlockchain;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Mikhail Kalinin
 * @since 28.07.2018
 */
public class ValidatorServiceTest {

    @Test
    public void testRegistration() throws InterruptedException {
        ShardingTestHelper.ShardingBootstrap bootstrap = ShardingTestHelper.bootstrap();

        ValidatorService validator = bootstrap.validatorService;
        StandaloneBlockchain blockchain = bootstrap.standaloneBlockchain;

        assertEquals(ValidatorService.State.Undefined, validator.getState());

        validator.init();
        assertEquals(ValidatorService.State.WaitForDeposit, validator.getState());

        // trigger sync done, it runs validator registration
        blockchain.getListener().onSyncDone(EthereumListener.SyncState.COMPLETE);
        blockchain.generatePendingTransactions();

        blockchain.createBlock();

        for (int i = 0; i < 10; i++) {
            if (!validator.getState().equals(ValidatorService.State.WaitForDeposit)) {
                break;
            }
            Thread.sleep(500);
        }

        assertEquals(validator.getState(), ValidatorService.State.Enlisted);

        // check validator restart
        ValidatorService cleanValidator = ShardingTestHelper.brandNewValidatorService(bootstrap);
        cleanValidator.init();

        assertEquals(cleanValidator.getState(), ValidatorService.State.Enlisted);
    }
}
