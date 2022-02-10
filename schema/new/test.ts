
import * as fs from 'fs';
import * as _ from 'lodash';

let a = [1,2,3,4,5,6]
console.log(_.zip(_.dropRight(a), _.drop(a)))