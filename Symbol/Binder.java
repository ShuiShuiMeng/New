package Symbol;

class Binder {
  Object value;
  Symbol prevtop;
  Binder tail;
  Binder(Object v, Symbol p, Binder t) {
	value=v; prevtop=p; tail=t;
  }
}