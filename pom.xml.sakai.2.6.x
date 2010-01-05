<?xml version="1.0"?>
<!--
/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2009 Etudes, Inc.
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
 *
 **********************************************************************************/
-->
<project xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns="http://maven.apache.org/POM/4.0.0"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<parent>
		<artifactId>base</artifactId>
		<groupId>org.sakaiproject</groupId>
		<!--version>M2</version-->
		<version>2.6-SNAPSHOT</version>
		<relativePath>../pom.xml</relativePath>
	</parent>

	
	<name>Etudes Util Project</name>
	<groupId>org.etudes.util</groupId>
	<artifactId>util</artifactId>
	<version>1.0</version>
	<packaging>pom</packaging>

	<modules>
		<module>etudes-util-api/api</module>
		<module>etudes-util/util</module>
	</modules>
</project>