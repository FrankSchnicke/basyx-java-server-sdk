/*******************************************************************************
 * Copyright (C) 2023 the Eclipse BaSyx Authors
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 * SPDX-License-Identifier: MIT
 ******************************************************************************/
package org.eclipse.digitaltwin.basyx.submodelservice.value.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.digitaltwin.aas4j.v3.model.Entity;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetID;
import org.eclipse.digitaltwin.basyx.submodelservice.value.EntityValue;
import org.eclipse.digitaltwin.basyx.submodelservice.value.SpecificAssetIDValue;

/**
 * Maps {@link Entity} value to {@link EntityValue}
 * 
 * @author danish
 *
 */
public class EntityValueMapper implements ValueMapper<EntityValue> {
	private Entity entity;

	public EntityValueMapper(Entity entity) {
		this.entity = entity;
	}

	@Override
	public EntityValue getValue() {
		return new EntityValue(ValueMapperUtil.createValueOnlyCollection(entity.getStatements()), entity.getEntityType(), entity.getGlobalAssetID(), getSpecificAssetIdValue(entity.getSpecificAssetIds()));
	}

	@Override
	public void setValue(EntityValue entityValue) {
		ValueMapperUtil.setValueOfSubmodelElementWithValueOnly(entity.getStatements(), entityValue.getStatements());
		entity.setEntityType(entityValue.getEntityType());
		setGlobalAssetID(entityValue.getGlobalAssetId());
		setSpecificAssetIDs(entityValue.getSpecificAssetIds());
	}

	private void setSpecificAssetIDs(List<SpecificAssetIDValue> specificAssetIdValues) {
		if (specificAssetIdValues == null)
			return;

		List<SpecificAssetID> specificAssetIds = specificAssetIdValues.stream().map(SpecificAssetIDValue::toSpecificAssetID).collect(Collectors.toList());
		entity.setSpecificAssetIds(specificAssetIds);
	}

	private void setGlobalAssetID(String value) {
		entity.setGlobalAssetID(value);
	}

	private List<SpecificAssetIDValue> getSpecificAssetIdValue(List<SpecificAssetID> specificAssetIds) {
		return specificAssetIds.stream().map(specificAssetId -> new SpecificAssetIDValue(specificAssetId)).collect(Collectors.toList());
	}
}
