const TEMPORARY_LOG = { // TODO: Demo Data until API is wired
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

export default TEMPORARY_LOG;
