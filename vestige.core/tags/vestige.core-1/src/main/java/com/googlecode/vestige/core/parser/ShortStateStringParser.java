/*
 * Copyright 2013 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlecode.vestige.core.parser;

import java.io.Serializable;

/**
 * @author Gael Lalire
 */
public class ShortStateStringParser implements StringParser, Serializable {

    private static final long serialVersionUID = -903818594895153214L;

    private char firstCharacter;

    private short[] characterIds;

    private short initialState;

    private short[] stateByCharacterIdAndState;

    private int statesNumber;

    private int[] data;

    private int defaultValue;

    public ShortStateStringParser(final char firstCharacter, final short[] characterIds, final short initialState, final short[] stateByCharacterIdAndState, final int statesNumber, final int[] data, final int defaultValue) {
        this.firstCharacter = firstCharacter;
        this.characterIds = characterIds;
        this.initialState = initialState;
        this.stateByCharacterIdAndState = stateByCharacterIdAndState;
        this.statesNumber = statesNumber;
        this.data = data;
        this.defaultValue = defaultValue;
    }

    public short nextState(final short state, final char c) {
        int pos = c - firstCharacter;
        if (pos < 0 || pos >= characterIds.length) {
            return 0;
        }
        short id = characterIds[pos];
        if (id < 0) {
            return 0;
        }
        return stateByCharacterIdAndState[id * statesNumber + state - 1];
    }

    public int match(final CharSequence sequence) {
        short state = initialState;
        for (int i = 0; i < sequence.length(); i++) {
            state = nextState(state, sequence.charAt(i));
            if (state == 0) {
                return defaultValue;
            }
            if (state < 0) {
                state = (short) (-state);
                break;
            }
        }
        if (state > data.length) {
            return defaultValue;
        }
        return data[state - 1];
    }

}
