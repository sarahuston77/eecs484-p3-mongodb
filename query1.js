// Query 1
// Find users who live in city "city".
// Return an array of user_ids. The order does not matter.

function find_user(city, dbname) {
    db = db.getSiblingDB(dbname);

    let results = [];
    // TODO: find all users who live in city
    results = db.users.find({"hometown.city" : city }, {user_id : 1}).map( 
        (p) => { return p.user_id }).toArray();
    
    // See test.js for a partial correctness check.
	print(results);
    return results;
}
