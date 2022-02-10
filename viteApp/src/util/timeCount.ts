export function countTime(label: string, fn : ()=>void) {
    console.time(label)
    fn()
    console.timeEnd(label)
}