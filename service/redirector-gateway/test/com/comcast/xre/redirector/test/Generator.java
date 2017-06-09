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
 */
package com.comcast.xre.redirector.test;

import java.util.UUID;

public class Generator {

	public static void main(String[] args) {
		for (int i = 0; i < 500; i++) {
			if (i % 25 == 0)
			{
				System.out.println();
			}
			String s = UUID.randomUUID().toString();
			System.out.print(s);
			System.out.print(",");
		}
		
		
	}

}
