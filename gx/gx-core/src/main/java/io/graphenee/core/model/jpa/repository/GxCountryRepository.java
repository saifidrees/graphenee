/*******************************************************************************
 * Copyright (c) 2016, 2018 Farrukh Ijaz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package io.graphenee.core.model.jpa.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import io.graphenee.core.model.entity.GxCountry;
import io.graphenee.core.model.jpa.GxJpaRepository;

@Repository
public interface GxCountryRepository extends GxJpaRepository<GxCountry, Integer> {

	List<GxCountry> findAllByIsActiveTrueOrderByCountryNameAsc();

	GxCountry findOneByIsActiveTrueAndNumericCodeOrderByCountryNameAsc(Integer numericCode);

	GxCountry findOneByIsActiveTrueAndAlpha3CodeOrderByCountryNameAsc(String alpha3Code);

	GxCountry findOneByIsActiveTrueAndCountryNameOrderByCountryNameAsc(String countryName);

	GxCountry findOneByIsActiveTrueAndGxStatesStateCodeOrderByCountryNameAsc(String stateCode);

	GxCountry findOneByIsActiveTrueAndGxStatesStateNameOrderByCountryNameAsc(String stateName);

	GxCountry findOneByIsActiveTrueAndGxCitiesCityNameOrderByCountryNameAsc(String cityName);
}
