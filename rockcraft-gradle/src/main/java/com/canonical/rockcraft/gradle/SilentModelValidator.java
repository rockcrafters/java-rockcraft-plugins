/*
 * Copyright 2025 Canonical Ltd.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.canonical.rockcraft.gradle;

import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.building.ModelProblemCollectorRequest;
import org.apache.maven.model.interpolation.DefaultModelVersionProcessor;
import org.apache.maven.model.validation.DefaultModelValidator;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.util.ArrayList;

/**
 * A model validator class that outputs warnings, but does not fail the model build.
 */
class SilentModelValidator extends DefaultModelValidator {

    private final Logger logger = Logging.getLogger(SilentModelValidator.class);

    private static class InternalModelProblemCollector implements ModelProblemCollector {

        private final ArrayList<ModelProblemCollectorRequest> requests = new ArrayList<>();

        @Override
        public void add(ModelProblemCollectorRequest modelProblemCollectorRequest) {
            requests.add(modelProblemCollectorRequest);
        }

        public ArrayList<ModelProblemCollectorRequest> getRequests() {
            return requests;
        }
    }

    public SilentModelValidator() {
        super(new DefaultModelVersionProcessor());
    }

    @Override
    public void validateRawModel(Model model, ModelBuildingRequest req, ModelProblemCollector collector) {
        InternalModelProblemCollector internalCollector = new InternalModelProblemCollector();
        super.validateRawModel(model, req, internalCollector);
        for (ModelProblemCollectorRequest problemReq : internalCollector.getRequests()) {
            logger.warn(problemReq.getMessage(), problemReq.getException());
        }
    }

    @Override
    public void validateEffectiveModel(Model model, ModelBuildingRequest req, ModelProblemCollector collector) {
        InternalModelProblemCollector internalCollector = new InternalModelProblemCollector();
        super.validateRawModel(model, req, internalCollector);
        for (ModelProblemCollectorRequest problemReq : internalCollector.getRequests()) {
            logger.warn(problemReq.getMessage(), problemReq.getException());
        }
    }
}
