# Differences in SQL Dialects

- SQLite
- MySQL/MariaDB
- SQL Server
- Oracle 12
- Oracle < 12

---

# Contents

- [Select](#select)
  - [Distinct](#distinct)
  - [Order By](#order-by)
  - [Limit](#limit)
  - [Min, Max, Count, Average, Sum](#min-max-count-average-sum)
- [Insert Into](#insert-into)
- [Update](#update)
- [Where](#where)
  - [And, Or, Not](#and-or-not)
  - [Null](#null)

## Select

All Dialects: `SELECT <columns> FROM <table>;`

> ### Distinct
> 
> All Dialects: `SELECT DISTINCT <columns> from <table>`
> 
> ### Order By
> 
> All Dialects:
> 
> `ORDER BY <columns> ASC|DESC`
> 
> `ORDER BY <column> ASC|DESC, ...`
> 
> ### Limit
> | Dialect                | Syntax                                                                                       |
> |------------------------|----------------------------------------------------------------------------------------------|
> | SQL Server             | `SELECT TOP <number> <columns> FROM <table>;`                                       |
> | MySQL, MariaDB         | `SELECT <columns> FROM <table> LIMIT <number>;`                                              |
> | Oracle 12              | `SELECT <columns> FROM <table> FETCH FIRST <number> ROWS ONLY;`                              |
> | Oracle < 12            | `SELECT <columns> FROM <table> WHERE ROWNUM <= <number>;`                                    |
> | Oracle < 12 (w/ ORDER) | `SELECT * FROM (SELECT <columns> FROM <table> ORDER BY <columns>) WHERE ROWNUM <= <number>;` |
> 
> ### Min, Max, Count, Average, Sum
> 
> All Dialects: `<function>(<column>)`
> 
> Valid Functions: `MIN`, `MAX`, `COUNT`, `AVG`, `SUM`
> 
> ### Aliasing
> 
> All Dialects: `<column> AS <alias>` or `<table> AS <alias>`
> 
> ### Joining
> 
> All Dialects:
> 
> | Join Type     | Action                                                              |
> |---------------|---------------------------------------------------------------------|
> | (INNER)       | All records matching in all tables                                  |
> | LEFT (OUTER)  | All records from left table, only matching records from right table |
> | RIGHT (OUTER) | All records from right table, only matching records from left table |
> | FULL (OUTER)  | All records matching either table                                   |
> 
>     SELECT <t1>.<c1>, <t2>.<c1>, ... FROM <t1>
>     [INNER|LEFT|RIGHT|FULL] JOIN <t2> ON <t1>.<related_column> = <t2>.<related_column>
> 
> Multiple Inner Joins may be used. Records must match in all tables to be returned. For example:
>
>     SELECT <t1>.<c1>, <t2>.<c1>, <t3>.<c1> ... FROM <t1>
>     INNER JOIN <t2> ON <t1>.<related_column> = <t2>.<related_column>
>     INNER JOIN <t3> ON <t1>.<related_column> = <t3>.<related_column>
> 
> ### Unions
> 
> Join multiple similar select statements. Must have the same number of columns and column types must match.
> 
> `UNION` selects only distinct values. To get values including duplicates, use `UNION ALL`
> 
>     <SELECT STATEMENT>
>     UNION
>     <SELECT STATEMENT>

## Insert Into

All Dialects: `INSERT INTO <table> (<columns>) VALUES (<values>)`

## Update

All Dialects: `UPDATE <table> SET <column> = <value>, ... WHERE <condition>`

***IMPORANT:*** Do not forget the `WHERE`, or the statement will affect **ALL** rows 

## Delete

All Dialects: `DELETE FROM <table> WHERE <condition>`

***IMPORANT:*** Do not forget the `WHERE`, or the statement will affect **ALL** rows

## Where

All Dialects: `SELECT <columns> FROM <table> WHERE <condition>`


> ### And, Or, Not
>
> All Dialects:
>
> `<condition> AND|OR <condition> ...`
>
> `<condition> AND (<condition> OR <condition> ...) ...`
>
> `NOT <condition>`
> 
> ### Null
> 
> All Dialects: `<column> IS [NOT ]NULL`
> 
> ### Like
> 
> All Dialects: `<column> LIKE <pattern>`
> 
> > #### Wildcards
> > 
> > | Function                          | SQL | Access | Example    | Match                       |
> > |-----------------------------------|-----|--------|------------|-----------------------------|
> > | 0+ of any character               | %   | *      | fo%ar      | foobar, foar, fobtar, ...   |
> > | Any character                     | _   | ?      | fo_bar     | foobar, foebar, foabar, ... |
> > | Any character in the brackets     | []  | []     | fo[oa]bar  | foobar, foabar              |
> > | Any character not in the brackets | ^   | !      | fo[!a]bar  | foobar, fobbar, ...         |
> > | Any character within the range    | -   | -      | be[a-c]ome | beaome, bebome, become      |
> > | Any single numeric character      | N/A | #      | 2#5        | 205, 215, ..., 295          |
> 
> ### In
> 
> All Dialects: `<column> IN (<value>, ...)` or `<column> IN (<SELECT STATEMENT>)`
> 
> ### Between
> 
> All Dialects: `<column> BETWEEN <value1> AND <value2>`
> 
> 