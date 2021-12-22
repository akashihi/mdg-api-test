const gulp = require('gulp');
const { series } = require('gulp');
const mocha = require('gulp-mocha');
const eslint = require('gulp-eslint');
var standard = require('gulp-standard')

function standardjs() {
    return gulp.src(['test/*.js'])
        .pipe(standard({fix: true, env: "mocha"}))
        .pipe(standard.reporter('default', {
            breakOnError: true,
            quiet: true
        }))
}

function lint() {
    return gulp.src(['test/*.js'])
        // eslint() attaches the lint output to the "eslint" property
        // of the file object so it can be used by other modules.
        .pipe(eslint())
        // eslint.format() outputs the lint results to the console.
        // Alternatively use eslint.formatEach() (see Docs).
        .pipe(eslint.format())
        // To have the process exit with an error code (1) on
        // lint error, return the stream and pipe to failAfterError last.
        .pipe(eslint.failAfterError());
}

function test() {
    return gulp.src('test/*.js', {read: false})
        // `gulp-mocha` needs filepaths so you can't have any plugins before it
        .pipe(mocha({reporter: 'list'}));
}

exports.default = series(standardjs, lint, test)