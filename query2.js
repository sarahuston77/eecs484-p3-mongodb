// Query 2
// Unwind friends and create a collection called 'flat_users' where each document has the following schema:
// {
//   user_id:xxx
//   friends:xxx
// }
// Return nothing.

function unwind_friends(dbname) {
    db = db.getSiblingDB(dbname);

    db.createCollection("flat_users")

    db.users.find().forEach( (u) => {
        let map = u.friends;
        map.forEach( (f) => {
            db.flat_users.insertOne({
                user_id : u.user_id,
                friends : f
            })
        }
        )
        return;  
    });


    return;
}
