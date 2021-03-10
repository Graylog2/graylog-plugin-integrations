/*
 * Copyright (C) 2020 Graylog, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */
import React from 'react';
import PropTypes from 'prop-types';

import lodash from "lodash";
import { Input } from 'components/bootstrap';


class GreyNoiseAdapterFieldSet extends React.Component {
  static propTypes = {
    config: PropTypes.shape({
      api_token: PropTypes.string.isRequired,
    }).isRequired,
    updateConfig: PropTypes.func.isRequired,
    handleFormEvent: PropTypes.func.isRequired,
    validationState: PropTypes.func.isRequired,
    validationMessage: PropTypes.func.isRequired,
  };

  handleSelect = () => {
    return (selectedIndicator) => {
      const config = lodash.cloneDeep(this.props.config);
      config[fieldName] = selectedIndicator;
      this.props.updateConfig(config);
    };
  };

  render() {
    const { config } = this.props;

    return (
      <fieldset>
          <Input type="password"
               id="api_token"
               name="api_token"
               label="API Token"
               onChange={this.props.handleFormEvent}
               help={this.props.validationMessage('api_token', 'The api token for the GreyNoise account.')}
               bsStyle={this.props.validationState('api_token')}
               value={config.api_token}
               labelClassName="col-sm-3"
               wrapperClassName="col-sm-9" />
      </fieldset>
    );
  }
}

export default GreyNoiseAdapterFieldSet;
