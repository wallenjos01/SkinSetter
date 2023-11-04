package org.wallentines.skinsetter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wallentines.midnightlib.types.ResettableSingleton;

public class SkinSetterAPI {

    public static final ResettableSingleton<SkinRegistry> REGISTRY = new ResettableSingleton<>();

    public static final Logger LOGGER = LoggerFactory.getLogger("SkinSetter");

}
