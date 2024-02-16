const config = {
    testEnvironment: 'node',
    sandboxInjectedGlobals: [
        'Math'
    ],
    reporters: [
        'default',
        ['jest-junit', {

        }],
    ]
}
module.exports = config;