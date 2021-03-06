/*

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

-- +goose Up
-- SQL in section 'Up' is executed when this migration is applied

CREATE TABLE IF NOT EXISTS deliveryservices_required_capability (
    required_capability TEXT NOT NULL,
    deliveryservice_id bigint NOT NULL,
    last_updated timestamp with time zone DEFAULT now() NOT NULL,

    PRIMARY KEY (deliveryservice_id, required_capability),
    CONSTRAINT fk_deliveryservice_id FOREIGN KEY (deliveryservice_id) REFERENCES deliveryservice(id) ON DELETE CASCADE,
    CONSTRAINT fk_required_capability FOREIGN KEY (required_capability) REFERENCES server_capability(name) ON DELETE RESTRICT
);

-- +goose Down
-- SQL section 'Down' is executed when this migration is rolled back
DROP TABLE IF EXISTS deliveryservices_required_capability;
