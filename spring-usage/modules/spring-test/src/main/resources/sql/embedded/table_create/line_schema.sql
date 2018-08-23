create table base_line(
	line_no integer primary key identity,
	line_code varchar(20),
	line_name varchar(20),
	is_run integer,
	modify_date datetime,
	dept_no integer
);