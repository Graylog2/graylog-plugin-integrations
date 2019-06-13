import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Button, Col, Row } from 'react-bootstrap';

<<<<<<< HEAD
=======
import { Button, Row, Col } from 'react-bootstrap';
>>>>>>> Cleanup
import { Input } from 'components/bootstrap';

export default class HealthCheck extends Component {
  static propTypes = {
    onSubmit: PropTypes.func.isRequired,
  }

  logOutput = {
    full_message: '2 123456789010 eni-abc123de 172.31.16.139 172.31.16.21 20641 22 6 20 4249 1418530010 1418530070 ACCEPT OK',
    version: 2,
    'account-id': 123456789010,
    'interface-id': 'eni-abc123de',
    src_addr: '172.31.16.139',
    dst_addr: '172.31.16.21',
    src_port: 20641,
    dst_port: 22,
    protocol: 6,
    packets: 20,
    bytes: 4249,
    start: 1418530010,
    end: 1418530070,
    action: 'ACCEPT',
    'log-status': 'OK',
  };

  render() {
    const { onSubmit } = this.props;

    return (
      <Row>
<<<<<<< HEAD
        <Col md={8}>
          <form onSubmit={onSubmit}>
            <h2>Create Kinesis Stream</h2>
            <p>We&apos;re going to attempt to parse a single log to help you out! If we&apos;re unable to, or you would like it parsed differently, head on over to <a href="/system/pipelines">Pipeline Rules</a> to set up your own parser!</p>

            <span><i className="fa fa-smile-o fa-2x" /> Great! Looks like a well formatted Flow Log.</span>

            <Input id="awsCloudWatchLog"
                   type="textarea"
                   label="Formatted CloudWatch Log"
                   value={JSON.stringify(this.logOutput, null, 2)}
                   disabled />
=======
        <Col md="8">
          <form onSubmit={onSubmit}>
            <h2>Create Kinesis Stream</h2>
            <p>We're going to attempt to parse a single log to help you out! If we're unable to, or you would like it parsed differently, head on over to <a href="/system/pipelines">Pipeline Rules</a> to set up your own parser!</p>

            <span><i className="fa fa-smile-o fa-2x" /> Great! Looks like a well formatted Flow Log.</span>

            <Input
              id="awsCloudWatchLog"
              type="textarea"
              label="Formatted CloudWatch Log"
              value={JSON.stringify(this.logOutput, null, 2)}
              disabled
            />
>>>>>>> Cleanup

            <Button type="submit">Review &amp; Finalize</Button>
          </form>
        </Col>
      </Row>
<<<<<<< HEAD
    );
=======
    )
>>>>>>> Cleanup
  }
}
