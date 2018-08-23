db.student.aggregate([ {
	$match : {
		birthDay : {
			$gt : new Date()
		},
		sex : "男",
		"friend.sex" : "男"
	}
}, {
	$limit : 3
}, {
	$sort : {
		sex : -1,
		age : 1
	}
}, {
	$addFields : {
		age : {
			$sum : [ "$_id", -50 ]
		}
	}
}, {
	$group : {
		_id : "$sex",
		allNames : {
			$push : "$name"
		},
		allDistinctName : {
			$addToSet : "$name"
		},
		ageSum : {
			$sum : "$age"
		}
	}
} ])