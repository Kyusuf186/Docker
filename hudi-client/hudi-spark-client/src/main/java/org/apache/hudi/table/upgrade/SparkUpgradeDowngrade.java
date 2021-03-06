/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hudi.table.upgrade;

import org.apache.hudi.common.config.ConfigProperty;
import org.apache.hudi.common.engine.HoodieEngineContext;
import org.apache.hudi.common.table.HoodieTableMetaClient;
import org.apache.hudi.common.table.HoodieTableVersion;
import org.apache.hudi.config.HoodieWriteConfig;
import org.apache.hudi.exception.HoodieUpgradeDowngradeException;

import java.io.IOException;
import java.util.Map;

public class SparkUpgradeDowngrade extends AbstractUpgradeDowngrade {

  public SparkUpgradeDowngrade(HoodieTableMetaClient metaClient, HoodieWriteConfig config, HoodieEngineContext context) {
    super(metaClient, config, context);
  }

  @Override
  public void run(HoodieTableMetaClient metaClient,
                  HoodieTableVersion toVersion,
                  HoodieWriteConfig config,
                  HoodieEngineContext context,
                  String instantTime) {
    try {
      new SparkUpgradeDowngrade(metaClient, config, context).run(toVersion, instantTime);
    } catch (IOException e) {
      throw new HoodieUpgradeDowngradeException("Error during upgrade/downgrade to version:" + toVersion, e);
    }
  }

  @Override
  protected Map<ConfigProperty, String> upgrade(HoodieTableVersion fromVersion, HoodieTableVersion toVersion, String instantTime) {
    if (fromVersion == HoodieTableVersion.ZERO && toVersion == HoodieTableVersion.ONE) {
      return new ZeroToOneUpgradeHandler().upgrade(config, context, instantTime);
    } else if (fromVersion == HoodieTableVersion.ONE && toVersion == HoodieTableVersion.TWO) {
      return new OneToTwoUpgradeHandler().upgrade(config, context, instantTime);
    } else {
      throw new HoodieUpgradeDowngradeException(fromVersion.versionCode(), toVersion.versionCode(), true);
    }
  }

  @Override
  protected Map<ConfigProperty, String> downgrade(HoodieTableVersion fromVersion, HoodieTableVersion toVersion, String instantTime) {
    if (fromVersion == HoodieTableVersion.ONE && toVersion == HoodieTableVersion.ZERO) {
      return new OneToZeroDowngradeHandler().downgrade(config, context, instantTime);
    } else if (fromVersion == HoodieTableVersion.TWO && toVersion == HoodieTableVersion.ONE) {
      return new TwoToOneDowngradeHandler().downgrade(config, context, instantTime);
    } else {
      throw new HoodieUpgradeDowngradeException(fromVersion.versionCode(), toVersion.versionCode(), false);
    }
  }
}
