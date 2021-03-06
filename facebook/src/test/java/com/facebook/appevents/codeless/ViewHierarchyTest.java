/**
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 *
 * You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
 * copy, modify, and distribute this software in source code or binary form for use
 * in connection with the web services and APIs provided by Facebook.
 *
 * As with any software that integrates with the Facebook platform, your use of
 * this software is subject to the Facebook Developer Principles and Policies
 * [http://developers.facebook.com/policy/]. This copyright notice shall be
 * included in all copies or substantial portions of the software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.facebook.appevents.codeless;

import android.content.Context;
import android.support.v4.view.NestedScrollingChild;
import android.view.View;
import android.view.ViewParent;
import android.widget.AdapterView;

import com.facebook.appevents.codeless.internal.ViewHierarchy;
import com.facebook.internal.Utility;

import org.json.JSONObject;
import org.junit.Test;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.lang.reflect.Method;

import static org.junit.Assert.assertTrue;

@PrepareForTest({
        ViewHierarchy.class,
})
public class ViewHierarchyTest extends CodelessTestBase {

    @Test
    public void testGetDictionaryOfView() throws Exception {
        JSONObject dict = ViewHierarchy.getDictionaryOfView(root);

        String outerText = dict.getJSONArray("childviews").getJSONObject(0).getString("text");
        assertTrue(outerText.equalsIgnoreCase(Utility.sha256hash("Outer Label")));

        String innerText =
                dict.getJSONArray("childviews")
                        .getJSONObject(1)
                        .getJSONArray("childviews")
                        .getJSONObject(0)
                        .getString("text");
        assertTrue(innerText.equalsIgnoreCase(Utility.sha256hash("Inner Label")));
    }

    static abstract class TestAdapterView extends AdapterView implements ViewParent {
        public TestAdapterView(Context context) {
            super(context);
        }
    }
    static abstract class TestNestedScrollingChild implements ViewParent, NestedScrollingChild {}
    @Mock View mockView;
    @Mock TestNestedScrollingChild mockTestNestedScrollingChild;
    @Mock TestAdapterView mockTestAdapterView;
    @Mock ViewParent mockViewParent;

    @Test
    public void testIsAdapterViewItem() throws Exception {
        PowerMockito.spy(ViewHierarchy.class);
        Method isAdapterViewItem = ViewHierarchy.class.getDeclaredMethod("isAdapterViewItem", View.class);
        isAdapterViewItem.setAccessible(true);

        // mock NestedScrollingChild -> true
        PowerMockito.when(mockView.getParent()).thenReturn(mockTestNestedScrollingChild);
        assertTrue((boolean) isAdapterViewItem.invoke(ViewHierarchy.class, mockView));

        // mock AdapterView -> true
        PowerMockito.when(mockView.getParent()).thenReturn(mockTestAdapterView);
        assertTrue((boolean) isAdapterViewItem.invoke(ViewHierarchy.class, mockView));

        // mock other cases -> false
        PowerMockito.when(mockView.getParent()).thenReturn(mockViewParent);
        assertTrue(!(boolean) isAdapterViewItem.invoke(ViewHierarchy.class, mockView));
    }
}
