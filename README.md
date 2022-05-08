# Coding challenge for Backend Development

See details here: http://blog.papauschek.com/2019/10/analytics-coding-challenge/


GET -> /products
utilises getProductsByCurrencyCode to fetch the corresponding currency code, which will convert Eur to currency using Rate conversion json multipliers

POST -> /purchases
will use extractCurrencyFromCurrencyCode and uses extra checks to make sure all fields are mandatory, test added for this

GET -> /purchases/statistics
tried to fix the bugs for failure cases, where in purchases might not have been made in the last 30 days
// also cleanup code a bit to avoid re-iterating over the purchases under 30 days to calculate sum,min and max,
// also earlier, max was also being evaluated as min, fixed that
// also extra checks to avoid ArrayIndexOutOfBounds exceptions.


