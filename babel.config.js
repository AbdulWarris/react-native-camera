module.exports = {
  presets: [
    ['@babel/preset-env', { targets: { node: 'current' } }],
    ['@babel/preset-react'],
  ],
  plugins: [
    '@babel/plugin-transform-flow-strip-types',
    '@babel/plugin-transform-class-properties',
  ],
};
