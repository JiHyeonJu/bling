/*
 * Copyright (C) 2019 Cricin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.cricin.colorpicker;

import android.view.View;

/**
 * A callback when color or alpha value changed.
 */
public interface OnValueChangeListener {
  /**
   * Called when a color or alpha is picked.
   *
   * @param view     Target view, either of {@link AlphaPicker}, {@link CircleColorPicker},
   *                 {@link ColorPicker} or {@link GrayPicker}
   * @param newValue A color with alpha channel filled with 0xFF, Or alpha value
   *                 ranges in [0, 255]
   */
  void onValueChanged(View view, int newValue);
}
