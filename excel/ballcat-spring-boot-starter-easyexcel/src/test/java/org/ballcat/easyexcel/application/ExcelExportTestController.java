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

package org.ballcat.easyexcel.application;

import org.ballcat.easyexcel.annotation.ResponseExcel;
import org.ballcat.easyexcel.annotation.Sheet;
import org.ballcat.easyexcel.head.EmptyHeadGenerator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hccake
 */
@RestController
@RequestMapping("export")
public class ExcelExportTestController {

	@ResponseExcel(name = "test", sheets = @Sheet(sheetName = "testSheet1"))
	@GetMapping("/simple")
	public List<DemoData> simple() {
		List<DemoData> dataList = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			DemoData data = new DemoData();
			data.setUsername("username" + i);
			data.setPassword("password" + i);
			dataList.add(data);
		}
		return dataList;
	}

	@ResponseExcel(name = "test-export", template = "template.xlsx", headGenerator = EmptyHeadGenerator.class)
	@GetMapping("/templateExportIgnoreHeader")
	public List<DemoData> templateExportIgnoreHeader() {
		List<DemoData> dataList = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			DemoData data = new DemoData();
			data.setUsername("username" + i);
			data.setPassword("password" + i);
			dataList.add(data);
		}
		return dataList;
	}

}
