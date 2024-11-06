/*
 * Copyright 2023-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballcat.easyexcel.fill;

/**
 * 填充数据提供者，在使用模板填充导出时使用，提供列表之外的数据。
 *
 * @author Hccake
 * @since 2.0.0
 */
public interface FillDataSupplier {

	/**
	 * 获取填充数据
	 * @return 填充数据
	 */
	Object getFillData();

}