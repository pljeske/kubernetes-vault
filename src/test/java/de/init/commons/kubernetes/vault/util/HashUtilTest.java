package de.init.commons.kubernetes.vault.util;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class HashUtilTest {

    @Test
    void testSameHashForSameValuesMap() throws Exception {
        Map<String,String> map1 = Map.of("key1", "value1", "key2", "value2");
        Map<String,String> map2 = Map.of("key1", "value1", "key2", "value2");

        String hash1 = HashUtil.getHash(map1);
        String hash2 = HashUtil.getHash(map2);

        assertEquals(hash1, hash2, "Hashcode of maps with same values was not equal");
    }

    @Test
    void testDifferentHashForUnequalValuesMap() throws Exception {
        Map<String,String> map1 = Map.of("key1", "value1", "key2", "value2");
        Map<String,String> map2 = Map.of("key3", "value1", "key2", "value2");

        String hash1 = HashUtil.getHash(map1);
        String hash2 = HashUtil.getHash(map2);

        assertNotEquals(hash1, hash2, "Hashcode of maps with unequal values was equal");
    }
}
