/*
 * Copyright 2018 Andrej Petras.
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
package org.lorislab.clingo4j.examples.rs;

import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.lorislab.clingo4j.api.Clingo;
import org.lorislab.clingo4j.api.ClingoException;
import org.lorislab.clingo4j.api.Model;
import org.lorislab.clingo4j.api.SolveHandle;
import org.lorislab.clingo4j.api.Symbol;

/**
 *
 * @author Andrej Petras
 */
@Stateless
@Path("service")
public class ClingoService {

    private static final Logger LOGGER = Logger.getLogger(ClingoService.class.getName());
    
    @POST
    @Path("solve")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject solve(String program) {
        LOGGER.info("clingo service solve start");
        JsonObjectBuilder b = Json.createObjectBuilder();
        
        Clingo.init();
        
        try (Clingo control = Clingo.create()) {
            control.add("base", program);
            control.ground("base");

            JsonArrayBuilder hb = Json.createArrayBuilder();

            try (SolveHandle handle = control.solve()) {
                for (Model model : handle) {

                    JsonObjectBuilder bm = Json.createObjectBuilder();
                    bm.add("type", model.getType().name());
                    JsonArrayBuilder ab = Json.createArrayBuilder();
                    
                    for (Symbol atom : model.getSymbols()) {
                        String tmp = atom.toString();
                        ab.add(tmp);
                    }
                    
                    bm.add("atoms", ab);
                    
                    hb.add(bm);
                }
            }

            b.add("models", hb);
            b.add("version", control.getVersion());
            
        } catch (ClingoException ex) {
            throw new RuntimeException(ex.getMessage());
        }

        LOGGER.info("clingo service finshed");
        return b.build();
    }
}
