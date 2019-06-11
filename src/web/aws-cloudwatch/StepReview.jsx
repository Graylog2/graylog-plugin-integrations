import React, { Component } from 'react'
import PropTypes from 'prop-types'

import { Button, Col } from 'react-bootstrap';

export default class Review extends Component {
  static propTypes = {
    onSubmit: PropTypes.func.isRequired,
    getAllValues: PropTypes.func.isRequired,
  }

  render() {
    const { getAllValues, onSubmit } = this.props;

    return (
      <form onSubmit={onSubmit}>
        <Col md={9} mdOffset={6}>
          <p></p>Review All The Things

          <code><pre>{JSON.stringify(getAllValues(), null, 2)}</pre></code>

          <Button type="submit">Complete CloudWatch Setup</Button>
        </Col>
      </form>
    )
  }
}
