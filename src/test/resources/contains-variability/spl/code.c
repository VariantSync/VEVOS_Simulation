// This is a test for the new ground truth format
# if FEATURE_A
int a = 0;
int b = 0;
# else if FEATURE_B || \
    FEATURE_C
int c = 0;
int d = 1;
#else
int e = 1;
int f = 1;
#endif
#if FEATURE_D
int g = 0;
int h = 0;
#endif
// common code
// EOF