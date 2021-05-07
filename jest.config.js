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
const fs = require('fs');
const buildConfig = require('./build.config');

const packageJson = JSON.parse(fs.readFileSync('package.json'));
const webSrcPrefix = buildConfig.web_src_path;
const {
  moduleDirectories,
  moduleNameMapper
} = packageJson.jest;

const jestConfig = {
  ...packageJson.jest,
  moduleDirectories: [].concat(moduleDirectories, [`${webSrcPrefix}/src`, `${webSrcPrefix}/test`]),
  moduleNameMapper: {
    ...moduleNameMapper,
    '^react$': `${webSrcPrefix}/node_modules/react/index.js`,
    '^react-dom$': `${webSrcPrefix}/node_modules/react-dom/index.js`,
    '^styled-components$': `${webSrcPrefix}/node_modules/styled-components`,
  },
};
module.exports = jestConfig;
